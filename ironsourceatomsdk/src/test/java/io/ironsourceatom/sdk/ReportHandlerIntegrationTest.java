package io.ironsourceatom.sdk;

import static io.ironsourceatom.sdk.TestsUtils.newReport;
import android.content.Context;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static junit.framework.Assert.*;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * ReportHandler integration with the StorageService.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 18, manifest = Config.NONE)
public class ReportHandlerIntegrationTest {

    @Before public void reset() {
        mClient.mBackedMock.clear();
    }

    @Test public void testPostSuccess() throws Exception {
        mHandler.handleReport(newReport(SdkEvent.POST_SYNC, event1));
        mHandler.handleReport(newReport(SdkEvent.POST_SYNC, event2));
        assertEquals(mClient.get(TABLE1), new JSONArray("[{" +
                "\"data\":\"ib-data\"," +
                "\"table\":\"ib_test\"," +
                "\"auth\":\"fbc254c2e706a3dc3a0b35985f220a66a2e05a25011bcbbe245671a2f54c1e8c\"" +
                "}]")
                .toString());
        assertEquals(mClient.get(TABLE2), new JSONArray("[{" +
                "\"data\":\"ic-data\"," +
                "\"table\":\"ic_test\"," +
                "\"auth\":\"bfcdf43b270ba2c1b19042f87bf094fe0c1b54f0be309d5451cfe52f18957189\"" +
                "}]")
                .toString());
    }

    @Test public void testPostFailed() {
        mClient.setNext(503);
        mHandler.handleReport(newReport(SdkEvent.POST_SYNC, event1));
        mHandler.handleReport(newReport(SdkEvent.POST_SYNC, event2));
        assertEquals(mAdapter.count(null), 2);
        assertEquals(mAdapter.getTables().size(), 2);
    }

    @Test public void testTrackEvent() {
        mConfig.setBulkSize(Integer.MAX_VALUE);
        for (int i = 1; i <= 10; i++) {
            mHandler.handleReport(newReport(SdkEvent.ENQUEUE, event1));
            assertEquals(mAdapter.count(null), i);
        }
        assertEquals(mAdapter.getTables().size(), 1);
    }

    @Test public void testTrackTriggerFlush() throws Exception {
        mConfig.setBulkSize(2);
        for (int i = 1; i <= 10; i++) {
            final Map<String, String> event = new HashMap<>(event1);
            event.put(ReportIntent.DATA, String.valueOf(i));
            mHandler.handleReport(newReport(SdkEvent.ENQUEUE, event));
        }
        assertEquals(mAdapter.count(null), 0);
        assertEquals(mAdapter.getTables().size(), 0);
        assertEquals(mClient.get(TABLE1), new JSONArray("[{" +
                "\"data\":\"[1, 2]\"," +
                "\"table\":\"ib_test\"," +
                "\"bulk\":true," +
                "\"auth\":\"a2fbb1365ac648437256831a43d8a127efe824c3b45564c4165bb4f68e42af08\"" +
                "}, {" +
                "\"data\":\"[3, 4]\"," +
                "\"table\":\"ib_test\"," +
                "\"bulk\":true," +
                "\"auth\":\"b46dcb43f024bb95c418d13c4f23712f59c7f934ad5d78fa49d763eaac1df2ed\"" +
                "}, {" +
                "\"data\":\"[5, 6]\"," +
                "\"table\":\"ib_test\"," +
                "\"bulk\":true," +
                "\"auth\":\"98da95e7b92ebdffd0307324cdcfe7df309e3666db1ccb6441afbf7a63f77a97\"" +
                "}, {" +
                "\"data\":\"[7, 8]\"," +
                "\"table\":\"ib_test\"," +
                "\"bulk\":true," +
                "\"auth\":\"187a7d74ad46e656fdda29543dc2ed8fee3954a064223ffa2046752d91c5c478\"" +
                "}, {" +
                "\"data\":\"[9, 10]\"," +
                "\"table\":\"ib_test\"," +
                "\"bulk\":true," +
                "\"auth\":\"1dee8cb3b7c482050b62582fe982ab50c7c49d0beddbc254d282fbe4feee897b\"" +
                "}]").toString());
    }

    @Test public void testFlush() {
        mConfig.setBulkSize(5);
        for (int i = 1; i <= 10; i++) {
            final Map<String, String> event = new HashMap<>(event1);
            event.put(ReportIntent.DATA, String.valueOf(i));
            mHandler.handleReport(newReport(SdkEvent.ENQUEUE, event));
            event.put(ReportIntent.TABLE, TABLE2);
            mHandler.handleReport(newReport(SdkEvent.ENQUEUE, event));
        }
        assertEquals(mAdapter.count(null), 0);
        assertEquals(mAdapter.getTables().size(), 0);
        assertEquals(mClient.mBackedMock.get(TABLE1).size(), 2);
        assertEquals(mClient.mBackedMock.get(TABLE2).size(), 2);
    }

    // Events to test
    final String TABLE1 = "ib_test", TOKEN1 = "ib_token", DATA1 = "ib-data";
    final String TABLE2 = "ic_test", TOKEN2 = "ic_token", DATA2 = "ic-data";
    final Map<String, String> event1 = new HashMap<String, String>(){{
        put(ReportIntent.DATA, DATA1);
        put(ReportIntent.TOKEN, TOKEN1);
        put(ReportIntent.TABLE, TABLE1);
    }};
    final Map<String, String> event2 = new HashMap<String, String>(){{
        put(ReportIntent.DATA, DATA2);
        put(ReportIntent.TOKEN, TOKEN2);
        put(ReportIntent.TABLE, TABLE2);
    }};
    // MockBackend
    final TestsUtils.MockPoster mClient = new TestsUtils.MockPoster();
    final IBConfig mConfig = IBConfig.getInstance(RuntimeEnvironment.application);
    final StorageService mAdapter = new DbAdapter(RuntimeEnvironment.application);
    final ReportHandler mHandler = new ReportHandler(RuntimeEnvironment.application) {
        @Override
        protected StorageService getStorage(Context context) { return mAdapter; }
        @Override
        protected RemoteService getClient() { return mClient; }
    };

}
