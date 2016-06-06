package io.ironsourceatom.sdk;

import android.content.Context;

import org.mockito.Mockito;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 18, manifest = Config.NONE)
public class ISAConfigTest {

    @Before public void reset() {
        Mockito.reset(mPrefService);
    }

    // When the custom url was not set previous, it should return the DEFAULT_URL.
    @Test public void testGetIBEndPoint() throws Exception {
        String token = "token";
        String prefKey = String.format("%s_%s", IsaConfig.KEY_IB_END_POINT, token);
        when(mPrefService.load(startsWith(prefKey))).thenReturn("");
        assertEquals(isaConfig.getISAEndPoint(token), IsaConfig.DEFAULT_URL);
        verify(mPrefService, times(1)).load(startsWith(prefKey));
    }

    // When the custom url was save previous but not loaded yet, it
    // should first load it from PreferencesService, store it in the
    // HashTable, and in the second time fetch it from there.
    @Test public void testGetIBEndPoint2Times() throws Exception {
        String token = "token";
        String customUrl = "http://foo.com/blah_blah";
        String prefKey = String.format("%s_%s", IsaConfig.KEY_IB_END_POINT, token);
        when(mPrefService.load(startsWith(prefKey))).thenReturn(customUrl);
        assertEquals(isaConfig.getISAEndPoint(token), customUrl);
        assertEquals(isaConfig.getISAEndPoint(token), customUrl);
        verify(mPrefService, times(1)).load(startsWith(prefKey));
    }

    // When the custom url was not set before, it should
    // return the DEFAULT_URL_BULK.
    @Test public void testGetIBEndPointBulk() throws Exception {
        String token = "token";
        when(mPrefService.load(anyString())).thenReturn("");
        assertEquals(isaConfig.getISAEndPointBulk(token), IsaConfig.DEFAULT_BULK_URL);
        assertEquals(isaConfig.getISAEndPointBulk(token), IsaConfig.DEFAULT_BULK_URL);
        verify(mPrefService, times(2)).load(anyString());
    }

    // When the custom url was save previous but not loaded yet, it
    // should first load it from PreferencesService, store it in the
    // HashTable, and in the second time fetch it from there.
    @Test public void testGetEndPointBulk2Times() throws Exception {
        String token = "token";
        String customUrl = "http://foo.com/blah_blah";
        String prefKey = String.format("%s_%s", IsaConfig.KEY_IB_END_POINT_BULK, token);
        when(mPrefService.load(startsWith(prefKey))).thenReturn(customUrl);
        assertEquals(isaConfig.getISAEndPointBulk(token), customUrl);
        assertEquals(isaConfig.getISAEndPointBulk(token), customUrl);
        verify(mPrefService, times(1)).load(anyString());
    }

    @Test public void testNotValidUrlWasSavedEndPoint() throws Exception {
        String token = "token";
        String customUrl = "blabla.com";
        String prefKey = String.format("%s_%s", IsaConfig.KEY_IB_END_POINT, token);
        when(mPrefService.load(startsWith(prefKey))).thenReturn(customUrl);
        assertEquals(isaConfig.getISAEndPoint(token), IsaConfig.DEFAULT_URL);
    }

    @Test public void testNotValidUrlWasSavedEndPointBulk() throws Exception {
        String token = "token";
        String customUrl = "blabla.com";
        String prefKey = String.format("%s_%s", IsaConfig.KEY_IB_END_POINT_BULK, token);
        when(mPrefService.load(startsWith(prefKey))).thenReturn(customUrl);
        assertEquals(isaConfig.getISAEndPointBulk(token), IsaConfig.DEFAULT_BULK_URL);
    }

    IsaPrefService mPrefService = mock(IsaPrefService.class);
    public IsaConfig isaConfig = new IsaConfig(RuntimeEnvironment.application) {
        @Override
        protected IsaPrefService getPrefService(Context context) {
            return mPrefService;
        }
    };
}
