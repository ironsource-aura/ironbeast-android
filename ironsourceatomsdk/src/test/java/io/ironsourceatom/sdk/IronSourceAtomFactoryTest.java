package io.ironsourceatom.sdk;

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

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

/**
 * Basic IronSourceAtomAPI test cases
 */
@RunWith(MockitoJUnitRunner.class)
public class IronSourceAtomFactoryTest {

    @Before public void Reset() {
        reset(mSpyReport);
    }

    @Test public void testGetInstance() {
        MockContext context = mock(MockContext.class);
        IronSourceAtomFactory ironSourceAtomFactory = IronSourceAtomFactory.getInstance(context);

        IronSourceAtomTracker tracker1 = ironSourceAtomFactory.newTracker("token1");
        IronSourceAtomTracker tracker2 = ironSourceAtomFactory.newTracker("token1");
        assertTrue("should not initialized new tracker with the same token", tracker1 == tracker2);
        IronSourceAtomTracker tracker3 = ironSourceAtomFactory.newTracker("token2");
        assertTrue("should initialized new tracker", tracker1 != tracker3 || tracker2 != tracker3);
    }

    @Test public void testGetAtom() {
        MockContext context = mock(MockContext.class);
        IronSourceAtomFactory ironSourceAtomFactory = IronSourceAtomFactory.getInstance(context);

        IronSourceAtom atom1 = ironSourceAtomFactory.newAtom("token1");
        IronSourceAtom atom2 = ironSourceAtomFactory.newAtom("token1");
        assertTrue("should not initialized new atoms with the same token", atom1 == atom2);
        IronSourceAtom atom3 = ironSourceAtomFactory.newAtom("token2");
        assertTrue("should initialized new atom", atom1 != atom3 || atom2 != atom3);
    }

    @Test public void trackStringEvent() {
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", "hello world");
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("hello world");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.ENQUEUE);
    }

    @Test public void trackJSONEvent() throws JSONException {
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

    @Test public void trackMapEvent() throws JSONException {
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

    @Test public void postStringEvent() {
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", "hello world", true);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("hello world");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.POST_SYNC);
    }

    @Test public void postJSONEvent() throws JSONException {
        JSONObject event = new JSONObject();
        event.put("hello", "world");
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", event, true);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("{\"hello\":\"world\"}");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.POST_SYNC);
    }

    @Test public void postMapEvent() throws JSONException {
        Map<String, String> event = new HashMap<>();
        event.put("hello", "world");
        for (int i = 0; i < 10; i++) {
            mTracker.track("table", event, true);
        }
        verify(mSpyReport, times(10)).setToken(mToken);
        verify(mSpyReport, times(10)).setTable("table");
        verify(mSpyReport, times(10)).setData("{\"hello\":\"world\"}");
        verify(mSpyReport, times(10)).send();
        assertEquals(mSpyReport.mType, SdkEvent.POST_SYNC);
    }

    @Test public void flushEvents() {
        mTracker.flush();
        verify(mSpyReport, times(1)).send();
        assertEquals(mSpyReport.mType, SdkEvent.FLUSH_QUEUE);
    }

    // Configure test
    final String mToken = "token";
    TestsUtils.MockReport mSpyReport = spy(new TestsUtils.MockReport());
    final IronSourceAtomTracker mTracker =  new IronSourceAtomTracker(mock(MockContext.class), mToken) {
        @Override
        public Report openReport(Context context, int event) {
            mSpyReport.mType = event;
            return mSpyReport;
        }
    };
    @Test
    public void trackErrorTest(){
        MockContext context = mock(MockContext.class);
        IronSourceAtomFactory ironSourceAtomFactory = IronSourceAtomFactory.getInstance(context);
        ironSourceAtomFactory.trackError("hdhdhd");

    }
}