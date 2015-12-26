package io.ironbeast.sdk;

import android.content.Context;
import android.test.mock.MockContext;

import junit.framework.TestCase;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class IBConfigTest extends TestCase {

    public void testGetInstance() throws Exception {
    }


    public void testGetIBEndPoint() throws Exception {
        String token = "token";
        when(mPrefService.load(anyString(), anyString())).thenReturn("0");
        assertEquals(mIBConfig.getIBEndPoint(token), IBConfig.DEFAULT_URL);
    }

    //The custom url was save previous but not loaded yet
    //Fist call loaded from Preferences
    //Second call fetched from HashTable

    public void testGetIBEndPoint2Times() throws Exception {
        String token = "token";
        String customUrl = "http://foo.com/blah_blah";
        String prefKey = String.format("%s_%s", IBConfig.KEY_IB_END_POINT, token);
        when(mPrefService.load(anyString(), anyString())).thenReturn(customUrl);

        assertEquals(mIBConfig.getIBEndPoint(token), customUrl);
        assertEquals(mIBConfig.getIBEndPoint(token), customUrl);
    }




    public void testGetIBEndPointBulk() throws Exception {
        String token = "token";
        when(mPrefService.load(anyString(), anyString())).thenReturn("0");
        assertEquals(mIBConfig.getIBEndPointBulk(token), IBConfig.DEFAULT_BULK_URL);
    }


    TestsUtils.MockPrefService mPrefService = spy(new TestsUtils.MockPrefService());
    public IBConfig mIBConfig = new IBConfig(mock(MockContext.class)) {
        @Override
        protected SharePrefService getPrefService(Context context) {
            return mPrefService;
        }
    };
}