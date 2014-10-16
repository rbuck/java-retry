package com.github.rbuck.retry;

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
    private int retryCount;

    public Incremental(int maxRetries, long initialInterval, long incrementalInterval) {
        this.maxRetries = maxRetries;
        this.initialInterval = initialInterval;
        this.incrementalInterval = incrementalInterval;
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
        return initialInterval + incrementalInterval * retryCount;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }
}
