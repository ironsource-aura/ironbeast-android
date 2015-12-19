package io.ironbeast.sdk;

import android.content.Context;
import android.test.mock.MockContext;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Basic IronBeastAPI test cases
 */
@RunWith(MockitoJUnitRunner.class)
public class IronBeastTest {

    @Before
    public void Reset() {
        reset(mSpyReport);
    }

    @Test
    public void testGetInstance() {
        MockContext context = mock(MockContext.class);
        IronBeast ironBeast = IronBeast.getInstance(context);

        IronBeastTracker tracker1 = ironBeast.newTracker("token1");
        IronBeastTracker tracker2 = ironBeast.newTracker("token1");
        assertTrue("should not initialized new tracker witht the same token", tracker1 == tracker2);
        IronBeastTracker tracker3 = ironBeast.newTracker("token2");
        assertTrue("should initialized new tracker", tracker1 != tracker3 || tracker2 != tracker3);
    }

    @Test
    public void trackStringEvent() {
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", "hello world");
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("hello world");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.ENQUEUE);
    }

    @Test
    public void trackJSONEvent() throws JSONException {
        JSONObject event = new JSONObject();
        event.put("hello", "world");
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", event);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("{\"hello\":\"world\"}");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.ENQUEUE);
    }

    @Test
    public void trackMapEvent() throws JSONException {
        Map<String, String> event = new HashMap<>();
        event.put("hello", "world");
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", event);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("{\"hello\":\"world\"}");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.ENQUEUE);
    }

    @Test
    public void postStringEvent() {
        for (int i = 0; i < 10; i++) {
            mTracker.post("table", "hello world");
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("hello world");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.POST_SYNC);
    }

    @Test
    public void postJSONEvent() throws JSONException {
        JSONObject event = new JSONObject();
        event.put("hello", "world");
        for (int i = 0; i < 10; i++) {
            mTracker.post("table", event);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("{\"hello\":\"world\"}");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.POST_SYNC);
    }

    @Test
    public void postMapEvent() throws JSONException {
        Map<String, String> event = new HashMap<>();
        event.put("hello", "world");
        for (int i = 0; i < 10; i++) {
            mTracker.post("table", event);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("{\"hello\":\"world\"}");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.POST_SYNC);
    }

    @Test
    public void flushEvents() {
        mTracker.flush();
        verify(mSpyReport, times(1)).send();
        assertEquals(mSpyReport.mType, SdkEvent.FLUSH_QUEUE);
    }

    // Configure test
    final String mToken = "token";
    TestsUtils.MockReport mSpyReport = spy(new TestsUtils.MockReport());
    final IronBeastTracker mTracker =  new IronBeastTracker(mock(MockContext.class), mToken) {
        @Override
        public Report openReport(Context context, int event) {
            mSpyReport.mType = event;
            return mSpyReport;
        }
    };
}