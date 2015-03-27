package com.github.rbuck.retry;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.github.rbuck.retry.RetryState.RetryStateCommon.addDelay;

/**
 * Implements truncated binary exponential backoff to calculate retry delay per
 * IEEE 802.3-2008 Section 1. There will be at most ten (10) contention periods
 * of backoff, each contention period whose amount is equal to the delta backoff.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class ExponentialBackoff implements RetryStrategy {

    public static final int DEFAULT_RETRY_COUNT = 10;
    public static final long DEFAULT_MIN_BACKOFF = TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);
    public static final long DEFAULT_MAX_BACKOFF = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
    public static final long DEFAULT_SLOT_TIME = TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS);

    private final Random random = new Random(1);
    private final int maxRetries;
    private final long minBackoff;
    private final long maxBackoff;
    private final long slotTime;

    public ExponentialBackoff() {
        this(DEFAULT_RETRY_COUNT, DEFAULT_MIN_BACKOFF, DEFAULT_MAX_BACKOFF, DEFAULT_SLOT_TIME);

    }

    public ExponentialBackoff(int maxRetries, long minBackoff, long maxBackoff, long slotTime) {
        this.maxRetries = maxRetries;
        this.minBackoff = minBackoff;
        this.maxBackoff = maxBackoff;
        this.slotTime = slotTime;
    }

    public ExponentialBackoff(int maxRetries) {
        this(maxRetries, DEFAULT_MIN_BACKOFF, DEFAULT_MAX_BACKOFF, DEFAULT_SLOT_TIME);
    }

    @Override
    public RetryState getRetryState() {
        return new RetryState() {

            private int retryCount;

            @Override
            public void delayRetry() {
                addDelay(getRetryDelay());
            }

            @Override
            public boolean hasRetries() {
                if (retryCount < maxRetries) {
                    retryCount++;
                    return true;
                }
                return false;
            }

            @Override
            public int getRetryCount() {
                return retryCount;
            }

            @Override
            public long getRetryDelay() {
                final int MAX_CONTENTION_PERIODS = 10;
                return retryCount == 0 ? 0 : Math.min(minBackoff + random.nextInt(2 << Math.min(retryCount, MAX_CONTENTION_PERIODS - 1)) * slotTime, maxBackoff);
            }
        };
    }
}
