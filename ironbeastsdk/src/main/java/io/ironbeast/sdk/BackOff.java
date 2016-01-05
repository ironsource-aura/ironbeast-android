package io.ironbeast.sdk;

import android.content.Context;
import java.util.concurrent.TimeUnit;

/**
 * Persistence exponential backoff service.
 */
class BackOff {

    BackOff(Context context) {
        mConfig = getConfig(context);
        mSharedPref = getPrefService(context);
        mRetry = mSharedPref.load(KEY_RETRY_COUNT, INITIAL_RETRY_VALUE);
    }

    public static BackOff getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (null == sInstance) {
                sInstance = new BackOff(context);
            }
        }
        return sInstance;
    }


    /**
     * Returns the next milliseconds and advances the counter.
     * @return
     */
    synchronized long next() {
        long nextTick, curr = currentTimeMillis();
        long lastTick = mSharedPref.load(KEY_LAST_TICK, curr);
        nextTick = curr + getMills(mRetry);
        if (curr - lastTick >= 0) {
            mSharedPref.save(KEY_RETRY_COUNT, ++mRetry);
        }
        mSharedPref.save(KEY_LAST_TICK, nextTick);
        return nextTick;
    }

    /**
     * Get milliseconds number based on the given n.
     * @param n
     * @return
     */
    private long getMills(int n) {
        if (n <= INITIAL_RETRY_VALUE) {
            return mConfig.getFlushInterval();
        }
        return TimeUnit.MINUTES.toMillis((int) Math.pow(2, n));
    }

    /**
     * Reset number of retries to INITIAL_RETRY_VALUE, and
     * save current state in sharedPreferences.
     */
    void reset() {
        mRetry = INITIAL_RETRY_VALUE;
        mSharedPref.save(KEY_RETRY_COUNT, mRetry);
        mSharedPref.save(KEY_LAST_TICK, currentTimeMillis());
    }

    public boolean hasNext() {
        return mRetry <= MAX_RETRY_COUNT;
    }

    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected long currentTimeMillis() { return System.currentTimeMillis(); }
    protected IBPrefService getPrefService(Context context) {
        return IBPrefService.getInstance(context);
    }
    protected IBConfig getConfig(Context context) {
        return IBConfig.getInstance(context);
    }

    private int mRetry;
    private IBConfig mConfig;
    private IBPrefService mSharedPref;
    private final String KEY_LAST_TICK = "retry_last_tick";
    private final String KEY_RETRY_COUNT = "retry_count";
    protected final int MAX_RETRY_COUNT = 7;
    protected final int INITIAL_RETRY_VALUE = -1;

    private static BackOff sInstance;
    private static final Object sInstanceLock = new Object();
}