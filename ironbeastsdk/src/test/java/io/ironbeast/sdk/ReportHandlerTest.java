package io.ironbeast.sdk;

import java.util.*;
import android.content.Context;
import android.content.Intent;
import android.test.mock.MockContext;
import org.json.JSONObject;
import org.mockito.runners.MockitoJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.ironbeast.sdk.TestsUtils.newReport;
import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import io.ironbeast.sdk.ReportHandler.HandleStatus;
import io.ironbeast.sdk.RemoteService.Response;
import io.ironbeast.sdk.StorageService.Batch;
import io.ironbeast.sdk.StorageService.Table;

@RunWith(MockitoJUnitRunner.class)
public class ReportHandlerTest {

    @Before
    public void startClear() {
        // reset mocks
        reset(mStorage, mPoster, mConfig);
        // add default configuration
        when(mConfig.getNumOfRetries()).thenReturn(1);
        when(mConfig.getAllowedNetworkTypes()).thenReturn(-1);
        when(mPoster.getNetworkIBType(mContext)).thenReturn(-1);
        when(mPoster.isOnline(mContext)).thenReturn(true);
    }

    @Test
    // When you tracking an event.
    public void trackOnly() throws Exception {
        mConfig.setBulkSize(Integer.MAX_VALUE);
        Intent intent = newReport(SdkEvent.ENQUEUE, reportMap);
        mHandler.handleReport(intent);
        verify(mStorage, times(1)).addEvent(mTable, DATA);
        verify(mPoster, never()).post(anyString(), anyString());
    }

    @Test
    // When handler get a post-event and everything goes well(connection available, and IronBeast responds OK).
    // Should call "isOnline()" and "post()" (with the given event), NOT add the event to the
    // persistence data storage, and returns true
    public void postSuccess() throws Exception {
        String url = "http://host.com/post";
        when(mPoster.post(anyString(), anyString())).thenReturn(ok);
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        when(mConfig.getIBEndPoint(anyString())).thenReturn(url);
        assertTrue(mHandler.handleReport(intent) == ReportHandler.HandleStatus.HANDLED);
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, times(1)).post(anyString(), eq(url));
        verify(mStorage, never()).addEvent(mTable, DATA);
    }

    @Test
    // When handler get a post-event and we get an authentication error(40X) from Poster
    // Should discard the event, NOT add it to thr storage and returns true.
    public void postAuthFailed() throws Exception {
        String url = "http://host.com";
        when(mConfig.getNumOfRetries()).thenReturn(10);
        when(mConfig.getIBEndPoint(TOKEN)).thenReturn(url);
        when(mPoster.post(anyString(), anyString())).thenReturn(new Response() {{
            code = 401;
            body = "Unauthorized";
        }});
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, times(1)).post(anyString(), eq(url));
        verify(mStorage, never()).addEvent(mTable, DATA);
    }

    @Test
    // When handler get a post-event(or flush), but the device not connected to internet.
    // Should try to post "n" times, add it to storage if it's failed, and returns false.
    public void postWithoutNetwork() throws Exception {
        when(mPoster.isOnline(mContext)).thenReturn(false);
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        // no idle time, but should try it out 10 times
        when(mConfig.getNumOfRetries()).thenReturn(10);
        assertEquals(mHandler.handleReport(intent), HandleStatus.RETRY);
        verify(mPoster, times(1)).isOnline(mContext);
        verify(mPoster, never()).post(anyString(), anyString());
        verify(mStorage, times(1)).addEvent(mTable, DATA);
    }

    @Test
    // When handler get a post-event(or flush), and the device is on ROAMING_MODE.
    // It should try to send only if its has a permission to it.
    public void postOnRoaming() throws Exception {
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        when(mConfig.isAllowedOverRoaming()).thenReturn(false, false, true);
        when(mPoster.isDataRoamingEnabled(mContext)).thenReturn(false, true, true);
        when(mPoster.post(anyString(), anyString())).thenReturn(ok);
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
        assertEquals(mHandler.handleReport(intent), HandleStatus.RETRY);
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
        verify(mPoster, times(2)).post(anyString(), anyString());
    }

    @Test
    // When handler get a post-event(or flush), should test if the
    // network type allowing it to make a network transaction before trying to make it.
    public void isNetworkTypeAllowed() throws Exception {
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        int WIFI = IronBeast.NETWORK_WIFI, MOBILE = IronBeast.NETWORK_MOBILE;
        // List of scenarios, each member contains:
        // configResult, networkTypeResult and the expected behavior.
        List<TestScenario> scenarios = new ArrayList<>();
        scenarios.add(new TestScenario(~0, MOBILE, HandleStatus.HANDLED));
        scenarios.add(new TestScenario(WIFI | MOBILE, MOBILE, HandleStatus.HANDLED));
        scenarios.add(new TestScenario(WIFI | MOBILE, WIFI, HandleStatus.HANDLED));
        scenarios.add(new TestScenario(WIFI, WIFI, HandleStatus.HANDLED));
        scenarios.add(new TestScenario(MOBILE, MOBILE, HandleStatus.HANDLED));
        scenarios.add(new TestScenario(WIFI, MOBILE, HandleStatus.RETRY));
        scenarios.add(new TestScenario(MOBILE, WIFI, HandleStatus.RETRY));
        when(mPoster.post(anyString(), anyString())).thenReturn(ok);
        for (TestScenario test: scenarios) {
            when(mConfig.getAllowedNetworkTypes()).thenReturn(test.configStatus);
            when(mPoster.getNetworkIBType(mContext)).thenReturn(test.networkStatus);
            assertEquals(mHandler.handleReport(intent), test.expected);
        }
    }

    @Test
    // When handler get a flush-event and there's no items in the queue.
    // Should do nothing and return true.
    public void flushNothing() {
        Intent intent = newReport(SdkEvent.FLUSH_QUEUE, new HashMap<String, String>());
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
        verify(mStorage, times(1)).getTables();
    }

    @Test
    // When handler get a flush-event, it should ask for the all tables with `getTables`,
    // and then call `getEvents` for each of them with `maximumBulkSize`.
    // If everything goes well, it should drain the table, and then delete it.
    public void flushSuccess() throws Exception {
        // Config this situation
        when(mConfig.getBulkSize()).thenReturn(2);
        when(mConfig.getMaximumRequestLimit()).thenReturn((long) (1024));
        // Another table to test
        final Table mTable1 = new Table("a8m", "a8m_token") {
            @Override
            public boolean equals(Object obj) {
                Table table = (Table) obj;
                return this.name.equals(table.name) && this.token.equals(table.token);
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
        // All success
        when(mPoster.post(anyString(), anyString())).thenReturn(ok, ok, ok);
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
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
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
        verify(mStorage, times(1)).getTables();
        verify(mStorage, never()).getEvents(any(Table.class), anyInt());
    }

    @Test
    // When handler try to flush a batch, and it encounter an error(e.g: connectivity)
    // should stop-flushing, and return false
    public void flushFailed() throws Exception {
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
        assertEquals(mHandler.handleReport(intent), HandleStatus.RETRY);
        verify(mStorage, times(1)).getEvents(mTable, mConfig.getBulkSize());
        verify(mStorage, never()).deleteEvents(mTable, "2");
        verify(mStorage, never()).deleteTable(mTable);
    }

    @Test
    // When tracking an event(record) to some table and the count number
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
        when(mConfig.getBulkSize()).thenReturn(2);
        when(mConfig.getMaximumRequestLimit()).thenReturn((long) (1024 * 1024 + 1));
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
    public void dataFormat() throws Exception {
        when(mPoster.post(any(String.class), any(String.class))).thenReturn(ok);
        Intent intent = newReport(SdkEvent.POST_SYNC, reportMap);
        assertEquals(mHandler.handleReport(intent), HandleStatus.HANDLED);
        JSONObject report = new JSONObject(reportMap);
        String token = reportMap.get(ReportIntent.TOKEN);
        report.put(ReportIntent.AUTH, Utils.auth(report.getString(ReportIntent.DATA),
                report.getString(ReportIntent.TOKEN))).remove(ReportIntent.TOKEN);
        verify(mPoster, times(1)).post(eq(report.toString()), anyString());
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
            return this.name.equals(table.name) && this.token.equals(table.token);
        }
    };
    // Two different responses
    final Response ok = new RemoteService.Response() {{ code = 200; body = "OK"; }};
    final Response fail = new RemoteService.Response() {{ code = 503; body = "Service Unavailable"; }};
    // Mocking
    final Context mContext = mock(MockContext.class);
    final StorageService mStorage = mock(DbAdapter.class);
    final RemoteService mPoster = mock(HttpService.class);
    final IBConfig mConfig = mock(IBConfig.class);
    final ReportHandler mHandler = new ReportHandler(mContext) {
        @Override
        protected RemoteService getPoster() { return mPoster; }
        @Override
        protected StorageService getStorage(Context context) { return mStorage; }
        @Override
        protected IBConfig getConfig(Context context) { return mConfig; }
    };

    // Helper class, used inside "isNetworkAllowed" test case.
    class TestScenario {
        int configStatus;
        int networkStatus;
        HandleStatus expected;

        TestScenario(int config, int network, HandleStatus exp) {
            configStatus = config;
            networkStatus = network;
            expected = exp;
        }
    }
}
