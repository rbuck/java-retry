package com.github.rbuck.retry;

/**
 * Represents current retry state.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class RetryEvent extends java.util.EventObject {

    private final int retryCount;
    private final long retryDelay;
    private final Exception cause;

    /**
     * Constructs a prototypical Event.
     *
     * @param source     The object on which the Event initially occurred.
     * @param retryDelay the delay for which the next retry will occur
     * @throws IllegalArgumentException if source is null.
     */
    public RetryEvent(Object source, int retryCount, long retryDelay, Exception cause) {
        super(source);
        this.retryCount = retryCount;
        this.retryDelay = retryDelay;
        this.cause = cause;
    }

    @Override
    public Object getSource() {
        return super.getSource();
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public Exception getCause() {
        return cause;
    }
}
