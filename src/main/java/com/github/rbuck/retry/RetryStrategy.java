package com.github.rbuck.retry;

/**
 * Interface for a retry strategy.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public interface RetryStrategy {
    /**
     * Creates a prototype state every time a request is made.
     *
     * @return instance of retry state
     */
    RetryState getRetryState();
}
