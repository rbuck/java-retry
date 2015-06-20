package com.github.rbuck.retry;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A generic retry policy.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class RetryPolicy<V> {

    private final RetryStrategy retryStrategy;
    private final TransientExceptionDetector transientExceptionDetector;

    /**
     * Implements a retry policy using the specified strategy and transient error detection algorithm.
     *
     * @param retryStrategy              the strategy that implements retry
     * @param transientExceptionDetector the transient error detection algorithm
     */
    public RetryPolicy(RetryStrategy retryStrategy, TransientExceptionDetector transientExceptionDetector) {
        this.retryStrategy = retryStrategy;
        this.transientExceptionDetector = transientExceptionDetector;
    }

    /**
     * Perform the specified action under the defined retry semantics.
     *
     * @param callable the action to perform under retry
     * @return the result of the action
     * @throws Exception inspect cause to determine reason, or interrupt status
     */
    public V action(Callable<V> callable) throws Exception {
        Exception re;
        RetryState retryState = retryStrategy.getRetryState();
        do {
            try {
                return callable.call();
            } catch (Exception e) {
                re = e;
                if (Thread.currentThread().isInterrupted() || isInterruptTransitively(e)) {
                    Thread.currentThread().interrupt();
                    re = new InterruptedException(e.getMessage());
                    break;
                }
                if (!transientExceptionDetector.isTransient(e)) {
                    break;
                }
            }
            enqueueRetryEvent(new RetryEvent(this, retryState, re));
            retryState.delayRetry();
        } while (retryState.hasRetries());
        throw re;
    }

    /**
     * Special case during shutdown.
     *
     * @param e possible instance of, or has cause for, an InterruptedException
     * @return true if it is transitively an InterruptedException
     */
    private boolean isInterruptTransitively(Throwable e) {
        do {
            if (e instanceof InterruptedException) {
                return true;
            }
            e = e.getCause();
        } while (e != null);
        return false;
    }

    private RetryEventListener[] retryListeners = new RetryEventListener[0];


    /**
     * Return this node's preference change listeners.  Even though we're using
     * a copy-on-write lists, we use synchronized accessors to ensure
     * information transmission from the writing thread to the reading thread.
     *
     * @return the property change listener list
     */
    private synchronized RetryEventListener[] retryListeners() {
        return retryListeners;
    }

    public synchronized void addRetryEventListener(RetryEventListener rel) {
        if (rel == null) {
            throw new IllegalArgumentException("Attempt to set null retry event listener");
        }

        // Copy-on-write
        RetryEventListener[] old = retryListeners;
        retryListeners = new RetryEventListener[old.length + 1];
        System.arraycopy(old, 0, retryListeners, 0, old.length);
        retryListeners[old.length] = rel;

        startEventDispatchThreadIfNecessary();
    }

    private static final List<RetryEvent> eventQueue = new LinkedList<>();

    private static class EventDispatchThread extends Thread {
        public void run() {
            while (true) {
                // Wait on eventQueue till an event is present
                RetryEvent event;
                synchronized (eventQueue) {
                    try {
                        while (eventQueue.isEmpty()) {
                            eventQueue.wait();
                        }
                        event = eventQueue.remove(0);
                    } catch (InterruptedException e) {
                        // never eat interrupts!
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                // Now we have event & hold no locks; deliver evt to listeners
                RetryPolicy src = (RetryPolicy) event.getSource();
                RetryEventListener[] listeners = src.retryListeners();
                for (RetryEventListener listener : listeners) {
                    listener.onRetry(event);
                }
            }
        }
    }

    private static Thread eventDispatchThread = null;

    private static synchronized void startEventDispatchThreadIfNecessary() {
        if (eventDispatchThread == null) {
            eventDispatchThread = new EventDispatchThread();
            eventDispatchThread.setDaemon(true);
            eventDispatchThread.start();
        }
    }

    private void enqueueRetryEvent(RetryEvent event) {
        if (retryListeners.length != 0) {
            synchronized (eventQueue) {
                eventQueue.add(event);
                eventQueue.notify();
            }
        }
    }

}
