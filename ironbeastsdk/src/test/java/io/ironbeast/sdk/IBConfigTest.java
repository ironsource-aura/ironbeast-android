package io.ironbeast.sdk;

import android.content.Context;
import android.test.mock.MockContext;

import junit.framework.TestCase;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IBConfigTest extends TestCase {

    TestsUtils.MockPrefService mPrefService = spy(new TestsUtils.MockPrefService());
    public IBConfig mIBConfig = new IBConfig(mock(MockContext.class)) {
        @Override
        protected SharePrefService getPrefService(Context context) {
            return mPrefService;
        }
    };

    public void testGetInstance() throws Exception {
    }

    /*
        The custom url was not set previous
        Will return DEFAULT_URL each time we request getIBEndPoint
    */
    public void testGetIBEndPoint() throws Exception {
        String token = "token";
        String prefKey = String.format("%s_%s", IBConfig.KEY_IB_END_POINT, token);

        when(mPrefService.load(startsWith(prefKey), anyString())).thenReturn("");
        assertEquals(mIBConfig.getIBEndPoint(token), IBConfig.DEFAULT_URL);
        assertEquals(mIBConfig.getIBEndPoint(token), IBConfig.DEFAULT_URL);
        verify(mPrefService, times(2)).load(startsWith(prefKey), anyString());
    }

/*
    The custom url was save previous but not loaded yet
    Fist call loaded from Preferences one time
    Second call fetched from HashTable
*/
    public void testGetIBEndPoint2Times() throws Exception {
        String token = "token";
        String customUrl = "http://foo.com/blah_blah";
        String prefKey = String.format("%s_%s", IBConfig.KEY_IB_END_POINT, token);
        when(mPrefService.load(startsWith(prefKey), anyString())).thenReturn(customUrl);

        assertEquals(mIBConfig.getIBEndPoint(token), customUrl);
        assertEquals(mIBConfig.getIBEndPoint(token), customUrl);
        verify(mPrefService, times(1)).load(startsWith(prefKey), anyString());
    }

    /*
    The custom url was not set previous
    Will return DEFAULT_URL_BULK each time we request getIBEndPointBulk
*/
    public void testGetIBEndPointBulk() throws Exception {
        String token = "token";
        String prefKey = String.format("%s_%s", IBConfig.KEY_IB_END_POINT_BULK, token);

        when(mPrefService.load(startsWith(prefKey), anyString())).thenReturn("");
        assertEquals(mIBConfig.getIBEndPointBulk(token), IBConfig.DEFAULT_BULK_URL);
        assertEquals(mIBConfig.getIBEndPointBulk(token), IBConfig.DEFAULT_BULK_URL);
        verify(mPrefService, times(2)).load(startsWith(prefKey), anyString());

    }

/*
    The custom url was save previous but not loaded yet
    Fist call loaded from Preferences one time
    Second call fetched from HashTable
*/
    public void testGetEndPointBulk2Times() throws Exception {
        String token = "token";
        String customUrl = "http://foo.com/blah_blah";
        String prefKey = String.format("%s_%s", IBConfig.KEY_IB_END_POINT_BULK, token);
        when(mPrefService.load(startsWith(prefKey), anyString())).thenReturn(customUrl);

        assertEquals(mIBConfig.getIBEndPointBulk(token), customUrl);
        assertEquals(mIBConfig.getIBEndPointBulk(token), customUrl);
        verify(mPrefService, times(1)).load(startsWith(prefKey), anyString());
    }
}