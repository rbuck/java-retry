package com.github.rbuck.retry;

/**
 * Internal retry state.
 */
public interface RetryState {
    /**
     * Delays retry according to the retry strategy.
     */
    void delayRetry();

    /**
     * Implements the retry policy.
     *
     * @return true if more retries are permitted, otherwise false
     */
    boolean hasRetries();

    /**
     * Gets the current retry count.
     *
     * @return the current retry count
     */
    int getRetryCount();

    /**
     * Get the retry interval to sleep for.
     *
     * @return the retry interval in milliseconds
     */
    long getRetryDelay();

    /**
     * Basic utilities for retry strategies.
     */
    public static class RetryStateCommon {
        /**
         * Delays the current thread the specified number of milliseconds.
         *
         * @param delay time delay in milliseconds
         */
        public static void addDelay(long delay) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
}
