package io.ironsourceatom.sdk;

import android.content.Context;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.ironsourceatom.sdk.BackOff;
import io.ironsourceatom.sdk.IBConfig;
import io.ironsourceatom.sdk.IBPrefService;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static junit.framework.Assert.*;


@RunWith(MockitoJUnitRunner.class)
public class BackOffTest {

    @Before public void clearMocks() {
        reset(mSharedPref, mConfig);
    }

    @Test public void testHasNext() {
        assertEquals(mBackOff.hasNext(), true);
    }

    @Test public void testFirstValue() {
        mBackOff.reset();
        int n = 99;
        when(mConfig.getFlushInterval()).thenReturn(n);
        assertEquals(mBackOff.next(), n);
    }

    @Test public void testNext() {
        mBackOff.reset();
        when(mConfig.getFlushInterval()).thenReturn(10000);
        when(mSharedPref.load(anyString(), anyLong())).thenReturn(0L);
        long t1 = mBackOff.next();
        mCurrentMills = t1;
        when(mSharedPref.load(anyString(), anyLong())).thenReturn(t1);
        long t2 = mBackOff.next();
        mCurrentMills = t2;
        when(mSharedPref.load(anyString(), anyLong())).thenReturn(t2);
        long t3 = mBackOff.next();
        assertTrue(t1 < t2 && t2 < t3);
        verify(mSharedPref, times(1)).save(anyString(), eq(t1));
        verify(mSharedPref, times(1)).save(anyString(), eq(t2));
        verify(mSharedPref, times(1)).save(anyString(), eq(t3));
        when(mSharedPref.load(anyString(), anyLong())).thenReturn(t3);
    }

    @Test public void testReset() {
        mBackOff.reset();
        verify(mSharedPref, times(1)).save(anyString(), eq(mBackOff.INITIAL_RETRY_VALUE));
        verify(mSharedPref, times(1)).save(anyString(), eq(mCurrentMills));
    }

    private long mCurrentMills = 0L;
    final Context mContext = mock(MockContext.class);
    final IBConfig mConfig = mock(IBConfig.class);
    final IBPrefService mSharedPref = mock(IBPrefService.class);
    BackOff mBackOff = new BackOff(mContext) {
        @Override
        protected IBPrefService getPrefService(Context context) { return mSharedPref; }
        @Override
        protected IBConfig getConfig(Context context) { return mConfig; }
        @Override
        protected long currentTimeMillis() { return mCurrentMills; }
    };
}
