package com.github.rbuck.retry;

import static com.github.rbuck.retry.RetryState.RetryStateCommon.addDelay;

/**
 * Represents a fixed interval RetryStrategy whose time interval is fixed.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class FixedInterval implements RetryStrategy {

    private final int maxRetries;
    private final long retryInterval;

    public FixedInterval(int maxRetries, long retryInterval) {
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
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
                return retryInterval;
            }
        };
    }
}
