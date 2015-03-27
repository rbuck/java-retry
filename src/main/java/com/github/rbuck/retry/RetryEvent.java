package com.github.rbuck.retry;

/**
 * Represents current retry state.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class RetryEvent extends java.util.EventObject {

    private final RetryState state;
    private final Exception cause;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @param state  the retry state
     * @throws IllegalArgumentException if source is null.
     */
    public RetryEvent(Object source, RetryState state, Exception cause) {
        super(source);
        this.state = state;
        this.cause = cause;
    }

    @Override
    public Object getSource() {
        return super.getSource();
    }

    public int getRetryCount() {
        return state.getRetryCount();
    }

    public long getRetryDelay() {
        return state.getRetryDelay();
    }

    public Exception getCause() {
        return cause;
    }
}
