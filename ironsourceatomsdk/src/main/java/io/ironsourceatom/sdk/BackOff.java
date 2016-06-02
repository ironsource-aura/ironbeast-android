package io.ironsourceatom.sdk;

import android.content.Context;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Persistence exponential backoff service.
 */
class BackOff {


    private int retry;
    private IsaConfig config;
    private IsaPrefService prefService;
    private final String KEY_LAST_TICK = "retry_last_tick";
    private final String KEY_RETRY_COUNT = "retry_count";
    protected final int MAX_RETRY_COUNT = 8;
    protected final int INITIAL_RETRY_VALUE = 0;

    private static BackOff sInstance;
    private static final Object sInstanceLock = new Object();

    BackOff(Context context) {
        config = getConfig(context);
        prefService = getPrefService(context);
        retry = prefService.load(KEY_RETRY_COUNT, INITIAL_RETRY_VALUE);
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
        long lastTick = prefService.load(KEY_LAST_TICK, curr);
        nextTick = curr + getMills(retry);
        if (curr - lastTick >= 0) {
            prefService.save(KEY_RETRY_COUNT, ++retry);
        }
        prefService.save(KEY_LAST_TICK, nextTick);
        return nextTick;
    }

    /**
     * Get milliseconds number based on the given n.
     * @param n
     * @return
     */
    private long getMills(int n) {
        if (n <= INITIAL_RETRY_VALUE) {
            return config.getFlushInterval();
        }
        return (long) (new Random().nextDouble() * TimeUnit.MINUTES.toMillis((int) Math.pow(2, n) - 1));
    }

    /**
     * Reset number of retries to INITIAL_RETRY_VALUE, and
     * save current state in sharedPreferences.
     */
    void reset() {
        retry = INITIAL_RETRY_VALUE;
        prefService.save(KEY_RETRY_COUNT, retry);
        prefService.save(KEY_LAST_TICK, currentTimeMillis());
    }

    public boolean hasNext() {
        return retry <= MAX_RETRY_COUNT;
    }

    /**
     * For testing purpose. to allow mocking this behavior.
     */
    protected long currentTimeMillis() { return System.currentTimeMillis(); }
    protected IsaPrefService getPrefService(Context context) {
        return IsaPrefService.getInstance(context);
    }
    protected IsaConfig getConfig(Context context) {
        return IsaConfig.getInstance(context);
    }

}