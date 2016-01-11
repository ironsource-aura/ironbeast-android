package io.ironbeast.sdk;

import org.junit.Before;
import org.junit.Test;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.mock.MockContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NetworkManagerTest {

    @Before public void startClear() {
        final ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(mNetworkInfo);
        when(mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
    }

    @Test public void isOnlineTest() {
        // #1
        when(mNetworkInfo.isConnected()).thenReturn(true);
        assertTrue(mNetManager.isOnline());
        // #2
        when(mNetworkInfo.isConnected()).thenReturn(false);
        assertFalse(mNetManager.isOnline());
        // #3
        when(mNetworkInfo.isConnected()).thenThrow(new SecurityException());
        assertTrue(mNetManager.isOnline());
    }

    @Test public void getNetworkTypeTest() {
        // #1
        when(mNetworkInfo.isConnected()).thenReturn(false);
        assertEquals(mNetManager.getConnectedNetworkType(), "unknown");
        // #2
        when(mNetworkInfo.isConnected()).thenReturn(true);
        when(mNetworkInfo.getTypeName()).thenReturn("a8m");
        assertEquals(mNetManager.getConnectedNetworkType(), "a8m");
    }

    @Test public void isDataRoamingEnabledTest() {
        // #1
        when(mNetworkInfo.isRoaming()).thenReturn(false);
        assertFalse(mNetManager.isDataRoamingEnabled());
        // #2
        when(mNetworkInfo.isRoaming()).thenReturn(true);
        assertTrue(mNetManager.isDataRoamingEnabled());
    }

    @Test public void getNetworkIBTypeTest() {
        // #1
        when(mNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        assertEquals(mNetManager.getNetworkIBType(), IronBeast.NETWORK_WIFI);
        // #2
        when(mNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        assertEquals(mNetManager.getNetworkIBType(), IronBeast.NETWORK_MOBILE);
        // #3
        when(mNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_BLUETOOTH);
        assertEquals(mNetManager.getNetworkIBType(), 0);
    }

    final Context mContext = mock(MockContext.class);
    final NetworkInfo mNetworkInfo = mock(NetworkInfo.class);
    final NetworkManager mNetManager = new NetworkManager(mContext);
}
