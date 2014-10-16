package com.github.rbuck.retry;

/**
 * Interface for receiving a retry event.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public interface RetryEventListener {

    /**
     * This method gets called when a retry occurs.
     *
     * @param evt The RetryEvent raised
     */
    void onRetry(RetryEvent evt);

}
