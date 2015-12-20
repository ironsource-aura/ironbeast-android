package io.ironbeast.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.mock.MockContext;

import io.ironbeast.sdk.RemoteService.Response;
import io.ironbeast.sdk.StorageService.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        reset(mStorage, mPoster);
    }

    @Test
    // When you tracking an event.
    public void trackOnly() throws Exception {
        mConfig.setBulkSize(Integer.MAX_VALUE);
        Intent intent = newReport(SdkEvent.ENQUEUE, reportMap);
        mHandler.handleReport(intent);
        verify(mStorage, times(1)).addEvent(mTable, DATA);
        verify(mPoster, never()).isOnline(mContext);
        verify(mPoster, never()).post(anyString(), anyString());
    }

    @Test
    // When handler get a post-event and everything goes well(connection available, and IronBeast responds OK).
    // Should call "isOnline()" and "post()" (with the given event), NOT add the event to the
    // persistence data storage, and returns true
    public void postSuccess() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(true);
        when(mPoster.post(any(String.class), any(String.class))).thenReturn(new Response() {{
            code = 200;
            body = "OK";
        }});
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertTrue(mHandler.handleReport(intent));
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, times(1)).post(anyString(), eq(mConfig.getIBEndPoint()));
        verify(mStorage, never()).addEvent(mTable, DATA);
    }

    @Test
    // When handler get a post-event and we get an authentication error(40X) from Poster
    // Should discard the event, NOT add it to thr storage and returns true.
    public void postAuthFailed() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(true);
        when(mPoster.post(anyString(), anyString())).thenReturn(new Response() {{
            code = 401;
            body = "Unauthorized";
        }});
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertTrue(mHandler.handleReport(intent));
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, times(1)).post(anyString(), eq(mConfig.getIBEndPoint()));
        verify(mStorage, never()).addEvent(mTable, DATA);
    }

    @Test
    // When handler get a post-event(or flush), but the device not connected to internet.
    // Should try to post "n" times, add it to storage if it's failed, and returns false.
    public void postWithoutNetwork() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(false);
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        // no idle time, but should try it out 10 times
        mConfig.setIdleSeconds(0).setNumOfRetries(10);
        assertFalse(mHandler.handleReport(intent));
        verify(mPoster, times(10)).isOnline(mContext);
        verify(mPoster, never()).post(anyString(), anyString());
        verify(mStorage, times(1)).addEvent(mTable, DATA);
    }

    @Test
    // When handler get a flush-event and there's no items in the queue.
    // Should do nothing and return true.
    public void flushNothing() {
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        assertTrue(mHandler.handleReport(intent));
        verify(mStorage, times(1)).getTables();
        verify(mPoster, never()).isOnline(mContext);
    }

    @Test
    // TODO: Add byte-limit test
    // When handler get a flush-event, it should ask for the all tables with `getTables`,
    // and then call `getEvents` for each of them with `maximumBulkSize`.
    // If everything goes well, it should drain the table, and then delete it.
    public void flushSuccess() throws Exception {
        // Config this situation
        mConfig.setBulkSize(2).setNumOfRetries(1).setIdleSeconds(0);
        // Another table to test
        final Table mTable1 = new Table("a8m", "a8m_token") {
            @Override
            public boolean equals(Object obj) {
                Table table = (Table) obj;
                return this.name == table.name && this.token == table.token;
            }
        };
        List<Table> tables = new ArrayList<Table>() {{ add(mTable); add(mTable1); }};
        when(mStorage.getTables()).thenReturn(tables);
        // mTable batch result
        when(mStorage.getEvents(mTable, mConfig.getBulkSize()))
                .thenReturn(new Batch("2", new ArrayList<String>() {{
                    add("foo");
                    add("bar");
                }}), new Batch("3", new ArrayList<String>() {{
                    add("foo");
                }}));
        // mTable1 batch result
        when(mStorage.getEvents(mTable1, mConfig.getBulkSize()))
                .thenReturn(new Batch("4", new ArrayList<String>() {{
                    add("foo");
                }}));
        when(mStorage.deleteEvents(mTable, "2")).thenReturn(2);
        when(mStorage.deleteEvents(mTable1, "4")).thenReturn(1);
        when(mStorage.count(mTable)).thenReturn(1);
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        // We're connected
        when(mPoster.isOnline(mContext)).thenReturn(true);
        // All success
        when(mPoster.post(anyString(), anyString())).thenReturn(ok, ok, ok);
        assertTrue(mHandler.handleReport(intent));
        verify(mStorage, times(2)).getEvents(mTable, mConfig.getBulkSize());
        verify(mStorage, times(1)).deleteEvents(mTable, "2");
        verify(mStorage, times(1)).deleteEvents(mTable, "3");
        // In the second and the third time, it assume that the table is empty
        // because NUMBER_OF_DELETES < NUMBER_OF_DESIRED
        verify(mStorage, times(1)).count(mTable);
        verify(mStorage, times(1)).deleteTable(mTable);
        verify(mStorage, times(1)).getEvents(mTable1, mConfig.getBulkSize());
        verify(mStorage, times(1)).deleteEvents(mTable1, "4");
        verify(mStorage, times(1)).deleteTable(mTable1);
    }

    @Test
    // When handler get a flush-event, and there's no tables to drain(i.e: no event)
    // Should do-nothing, and return true
    public void flushNoItems() throws Exception {
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        assertTrue(mHandler.handleReport(intent));
        verify(mStorage, times(1)).getTables();
        verify(mStorage, never()).getEvents(any(Table.class), anyInt());
    }

    @Test
    // When handler try to flush a batch, and it encounter an error(e.g: connectivity)
    // should stop-flushing, and return false
    public void flushFailed() throws Exception {
        // Config this situation
        mConfig.setBulkSize(2).setNumOfRetries(1).setIdleSeconds(0);
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        // Batch result
        when(mStorage.getEvents(mTable, mConfig.getBulkSize()))
                .thenReturn(new Batch("2", new ArrayList<String>() {{
                    add("foo");
                    add("bar");
                }}));
        when(mStorage.getTables()).thenReturn(new ArrayList<Table>() {{
            add(mTable);
        }});
        when(mPoster.post(anyString(), anyString())).thenReturn(fail);
        assertFalse(mHandler.handleReport(intent));
        verify(mStorage, times(1)).getEvents(mTable, mConfig.getBulkSize());
        verify(mStorage, never()).deleteEvents(mTable, "2");
        verify(mStorage, never()).deleteTable(mTable);
    }

    @Test
    // When tracking an event(record) to some table and the count numbder
    // is greater or equal to bulk-size, should flush the queue.
    public void trackCauseFlush() {
        mConfig.setBulkSize(2);
        when(mStorage.addEvent(mTable, DATA)).thenReturn(2);
        Intent intent = newReport(SdkEvent.ENQUEUE, reportMap);
        mHandler.handleReport(intent);
        verify(mStorage, times(1)).addEvent(mTable, DATA);
        verify(mStorage, times(1)).getEvents(mTable, mConfig.getBulkSize());
    }

    @Test
    // ByteSize limits logic
    // The scenario goes like this:
    // Ask for events with limit of 2 and the batch is too large.
    // handler decrease the bulkSize(limit) and ask for limit of 1.
    // in this situation it doesn't have another choice except sending this batch(of length 1).
    public void maxRequestLimit() throws Exception {
        mConfig.setMaximumRequestLimit(1024 * 1024 + 1).setBulkSize(2).setIdleSeconds(0);
        final String chunk = new String(new char[1024 * 1024]).replace('\0', 'b');
        when(mStorage.getTables()).thenReturn(new ArrayList<Table>() {{
            add(mTable);
        }});
        when(mStorage.getEvents(eq(mTable), anyInt())).thenReturn(new Batch("2", new ArrayList<String>() {{
            add(chunk);
            add(chunk);
        }}), new Batch("1", new ArrayList<String>() {{
            add(chunk);
        }}), new Batch("2", new ArrayList<String>() {{
            add(chunk);
        }}));
        when(mStorage.deleteEvents(eq(mTable), anyString())).thenReturn(1);
        when(mStorage.count(mTable)).thenReturn(1, 0);
        when(mPoster.post(anyString(), anyString())).thenReturn(ok, ok);
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        mHandler.handleReport(intent);
        verify(mStorage, times(2)).getEvents(mTable, 2);
        verify(mStorage, times(1)).getEvents(mTable, 1);
        verify(mStorage, times(1)).deleteTable(mTable);
    }

    @Test
    // Test data format
    // Should omit the "token" field and add "auth"
    public void dataFormat() throws Exception{
        when(mPoster.isOnline(mContext)).thenReturn(true);
        when(mPoster.post(any(String.class), any(String.class))).thenReturn(ok);
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertTrue(mHandler.handleReport(intent));
        JSONObject report = new JSONObject(reportMap);
        report.put(ReportIntent.AUTH, Utils.auth(report.getString(ReportIntent.DATA),
                report.getString(ReportIntent.TOKEN))).remove(ReportIntent.TOKEN);
        verify(mPoster, times(1)).post(eq(report.toString()), eq(mConfig.getIBEndPoint()));
    }

    // Helper method.
    // Take SdkEvent and Map and generate new MockReport
    private Intent newReport(int event, Map<String, String> report) {
        Intent intent = mock(Intent.class);
        when(intent.getIntExtra(ReportIntent.EXTRA_SDK_EVENT, SdkEvent.ERROR))
                .thenReturn(event);
        Bundle bundle = mock(Bundle.class);
        for (String key: report.keySet()) when(bundle.get(key)).thenReturn(report.get(key));
        when(intent.getExtras()).thenReturn(bundle);
        return intent;
    }

    // Constant report arguments for testing
    final String TABLE = "ib_table", TOKEN = "ib_token", DATA = "hello world";
    final Map<String, String> reportMap = new HashMap<String, String>(){{
        put(ReportIntent.DATA, DATA);
        put(ReportIntent.TOKEN, TOKEN);
        put(ReportIntent.TABLE, TABLE);
    }};
    final Table mTable = new Table(TABLE, TOKEN) {
        @Override
        public boolean equals(Object obj) {
            Table table = (Table) obj;
            return this.name == table.name && this.token == table.token;
        }
    };
    // Two different responses
    final Response ok = new RemoteService.Response() {{ code = 200; body = "OK"; }};
    final Response fail = new RemoteService.Response() {{ code = 503; body = "Service Unavailable"; }};
    // Mocking
    final Context mContext = mock(MockContext.class);
    final IBConfig mConfig = IBConfig.getInstance(mContext);
    final StorageService mStorage = mock(DbAdapter.class);
    final RemoteService mPoster = spy(new TestsUtils.MockPoster());
    final ReportHandler mHandler = new ReportHandler(mContext) {
        @Override
        protected RemoteService getPoster() { return mPoster; }
        @Override
        protected StorageService getStorage(Context context) { return mStorage; }
    };
}
