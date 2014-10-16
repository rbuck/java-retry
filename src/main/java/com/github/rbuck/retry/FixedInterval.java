package com.github.rbuck.retry;

/**
 * Represents a fixed interval RetryStrategy whose time interval is fixed.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class FixedInterval implements RetryStrategy {

    private final int maxRetries;
    private final long retryInterval;
    private int retryCount;

    public FixedInterval(int maxRetries, long retryInterval) {
        this.maxRetries = maxRetries;
        this.retryInterval = retryInterval;
    }

    @Override
    public boolean permitsRetry() {
        if (retryCount < maxRetries) {
            retryCount++;
            return true;
        }
        return false;
    }

    @Override
    public long getRetryDelay() {
        return retryInterval;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

}
