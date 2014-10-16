package com.github.rbuck.retry;

/**
 * Defines an interface to detect for transient error conditions.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public interface TransientExceptionDetector {
    /**
     * Determine if the condition is a transient error.
     *
     * @param e the exception to inspect
     * @return true if the exception is transient, false otherwise
     */
    boolean isTransient(Exception e);
}
