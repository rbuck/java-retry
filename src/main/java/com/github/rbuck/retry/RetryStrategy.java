package com.github.rbuck.retry;

/**
 * Interface for a retry strategy.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public interface RetryStrategy {

    /**
     * Implements the retry policy.
     *
     * @return true if more retries are permitted, otherwise false
     */
    boolean permitsRetry();

    /**
     * Get the retry interval to sleep for.
     *
     * @return the retry interval in milliseconds
     */
    long getRetryDelay();

    /**
     * Gets the current retry count.
     */
    int getRetryCount();
}
