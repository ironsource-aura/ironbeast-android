package com.ironsource.mobilcore;

import com.ironsource.mobilcore.RemoteService.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.mock.MockContext;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

/**
 * ReportHandler unit test. should cover:
 * 1. Getting single post-event, should "Post" it to IronBeast
 *    to "IronBeastEndPoint"
 *    - if it's success, we're cool.
 *    - if it's fail, should "write/push" it to StorageService
 * 2. Getting track-event, should write it to StorageService,
 *    - if the StorageService.count() is greater or equal to "BulkSize" should
 *      flush(see below) the queue.
 * 3. Flush should drain the queue and map the entries based on the destination/table field.
 *    then, send each "bulk" separately to "IronBestBulkEndPoint".
 *    if the bulk is "too big(bytes/or bulkSize)", we split it into chunks.
 *    if some of the "bulk-request" failed(500>=), we put all its entries back to the queue.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportHandlerTest {

    @Before
    public void clearMocks() {
        reset(mQueue, mPoster);
    }

    // When you tracking an event.
    @Test
    public void trackOnly() {
        mConfig.setBulkSize(Integer.MAX_VALUE);
        Intent intent = newReport(SdkEvent.ENQUEUE, reportMap);
        mHandler.handleReport(mContext, intent);
        verify(mQueue, times(1)).push(reportString);
    }

    @Test
    // When handler get a post-event and everything goes well(connection available, and IronBeast responds OK).
    // Should call "isOnline()" and "post()" (with the given event), NOT push the event to queue and
    // returns true
    public void postSuccess() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(true);
        when(mPoster.post(any(String.class), any(String.class))).thenReturn(new Response() {{
            code = 200;
            body = "OK";
        }});
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertTrue(mHandler.handleReport(mContext, intent));
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, times(1)).post(any(String.class), eq(mConfig.getIBEndPoint()));
        verify(mQueue, never()).push(reportString);
    }

    @Test
    // When handler get a post-event and we get an authentication error(40X) from Poster
    // Should discard the event, NOT push it to queue and returns true.
    public void postAuthFailed() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(true);
        when(mPoster.post(any(String.class), any(String.class))).thenReturn(new Response() {{
            code = 401;
            body = "Unauthorized";
        }});
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertTrue(mHandler.handleReport(mContext, intent));
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, times(1)).post(any(String.class), eq(mConfig.getIBEndPoint()));
        verify(mQueue, never()).push(reportString);
    }

    @Test
    // When handler get a post-event(or flush), but the device not connection to internet.
    // Should try to post "n" times, put it in the queue if it's failed and returns false.
    public void postWithoutNetwork() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(false);
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        // no idle time, but should try out 10 times
        mConfig.setIdleSeconds(0).setNumOfRetries(10);
        assertFalse(mHandler.handleReport(mContext, intent));
        verify(mPoster, times(10)).isOnline(mContext);
        verify(mPoster, never()).post(anyString(), anyString());
        verify(mQueue, times(1)).push(reportString);
    }

    @Test
    // When handler get a flush-event and there's no items in the queue.
    // Should do nothing and return true.
    public void flushNothing() {
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        assertTrue(mHandler.handleReport(mContext, intent));
        verify(mQueue, times(1)).peek();
        verify(mPoster, never()).isOnline(mContext);
    }

    @Test
    // TODO: Implement "/bulk" route solution in IBConfig
    // When handler get a flush-event and there's items in the queue.
    // Should "peek()" these items, map them per destination(table field)
    // and send each of the bulks separately to "mConfig.IronBestBulkEndPoint".
    // if the bulk is "too big(bytes/or bulkSize)", we split it into chunks(many bulks with the same destination).
    // if some of the "bulk-request" failed(500>=), we put all its entries back to the queue, and clear all the others.
    public void flushReports() throws Exception {
        // One more report, with different destination
        String reportString1 = new JSONObject(reportString).put(ReportIntent.TABLE, "a8m").toString();
        when(mQueue.peek()).thenReturn(new String[]{reportString1, reportString1, reportString, reportString});
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        // Config this situation
        mConfig.setBulkSize(1).setNumOfRetries(1).setIdleSeconds(0);
        // Two different responses
        Response ok  = new RemoteService.Response() {{ code = 200; body = "OK"; }};
        Response fail  = new RemoteService.Response() {{ code = 500; body = "Internal Error"; }};
        // We're connected
        when(mPoster.isOnline(mContext)).thenReturn(true);
        // All success
        when(mPoster.post(anyString(), anyString())).thenReturn(ok, ok, ok, ok);
        assertTrue(mHandler.handleReport(mContext, intent));
        verify(mQueue, never()).push(anyString());
        verify(mQueue, times(1)).clear();
        // Half-success, half-failed
        when(mPoster.post(anyString(), anyString())).thenReturn(ok, fail, ok, fail);
        assertFalse(mHandler.handleReport(mContext, intent));
        verify(mQueue, times(1)).push(reportString1, reportString);
        // All failed
        when(mPoster.post(anyString(), anyString())).thenReturn(fail, fail, fail, fail);
        assertFalse(mHandler.handleReport(mContext, intent));
        verify(mQueue, times(1)).push(reportString1, reportString1, reportString, reportString);
    }

    @Test
    // When tracking a track-event, and the queue-size is greater or equal to
    // bulk-size, should flush the queue.
    public void trackCauseFlush() {
        mConfig.setBulkSize(1);
        when(mQueue.push(anyString())).thenReturn(1);
        Intent intent = newReport(SdkEvent.ENQUEUE, reportMap);
        mHandler.handleReport(mContext, intent);
        verify(mQueue, times(1)).push(reportString);
        // peek() before draining
        verify(mQueue, times(1)).peek();
    }

    @Test
    // Test data format
    // Should omit the "token" field and add "auth"
    public void dataFormat() throws Exception{
        when(mPoster.isOnline(mContext)).thenReturn(true);
        when(mPoster.post(any(String.class), any(String.class))).thenReturn(new Response() {{
            code = 200;
            body = "OK";
        }});
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertTrue(mHandler.handleReport(mContext, intent));
        JSONObject report = new JSONObject(reportString);
        report.put(ReportIntent.AUTH, Utils.auth(report.getString(ReportIntent.DATA),
                report.getString(ReportIntent.TOKEN))).remove(ReportIntent.TOKEN);
        verify(mPoster, times(1)).post(eq(report.toString()), eq(mConfig.getIBEndPoint()));
    }

    // Helper method.
    // Take SdkEvent and Map and generate new MockReport
    private Intent newReport(int event, Map<String, String> report) {
        Intent intent = mock(Intent.class);
        when(intent.getIntExtra(ReportIntent.EXTRA_REPORT_TYPE, SdkEvent.ERROR))
                .thenReturn(event);
        Bundle bundle = mock(Bundle.class);
        for (String key: report.keySet()) when(bundle.get(key)).thenReturn(report.get(key));
        when(intent.getExtras()).thenReturn(bundle);
        return intent;
    }

    // Constant report arguments for testing
    final Map<String, String> reportMap = new HashMap<String, String>(){{
        put(ReportIntent.DATA, "data");
        put(ReportIntent.TOKEN, "token");
        put(ReportIntent.TABLE, "table");
    }};
    // The stringified report
    final String reportString = new JSONObject(reportMap).toString();
    // Mocking
    final Context mContext = mock(MockContext.class);
    final IBConfig mConfig = IBConfig.getsInstance();
    final StorageService mQueue = spy(new TestsUtils.MockQueue());
    final RemoteService mPoster = spy(new TestsUtils.MockPoster());
    final ReportHandler mHandler = new ReportHandler() {
        @Override
        protected RemoteService getPoster() { return mPoster; }
        @Override
        protected StorageService getQueue(String filename, Context context) { return mQueue; }
    };
}
