package com.ironsource.mobilcore;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.mock.MockContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class HttpServiceTest {

    @Test
    // Test isOnline behavior
    public void isOnlineTest() throws Exception {
        final ConnectivityManager connectivityManager = mock(ConnectivityManager.class);
        final NetworkInfo networkInfo = mock(NetworkInfo.class);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        // #1
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);
        assertTrue(mPoster.isOnline(mContext));
        // #2
        when(networkInfo.isConnectedOrConnecting()).thenReturn(false);
        assertFalse(mPoster.isOnline(mContext));
        // #3
        when(networkInfo.isConnectedOrConnecting()).thenThrow(new SecurityException());
        assertTrue(mPoster.isOnline(mContext));
    }

    @Test
    // Test post behavior
    public void postTest() {

    }

    // Mocking
    final Context mContext = mock(MockContext.class);
    final HttpService mPoster = HttpService.getInstance();
}
