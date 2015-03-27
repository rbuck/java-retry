package com.github.rbuck.retry;

import static com.github.rbuck.retry.RetryState.RetryStateCommon.addDelay;

/**
 * Implementation for an incremental retry strategy with fixed initial and
 * incremental wait times.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class Incremental implements RetryStrategy {

    private final int maxRetries;
    private final long initialInterval;
    private final long incrementalInterval;

    public Incremental(int maxRetries, long initialInterval, long incrementalInterval) {
        this.maxRetries = maxRetries;
        this.initialInterval = initialInterval;
        this.incrementalInterval = incrementalInterval;
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
                return initialInterval + incrementalInterval * retryCount;
            }
        };
    }
}
