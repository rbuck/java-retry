package com.github.rbuck.retry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.concurrent.Callable;

/**
 * Tests the RetryPolicy class.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class RetryPolicyTest {

    private int retryCount = 0;
    private int eventCount = 0;

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

    @Test
    public void testRetryEventListenerIsNull() {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return e instanceof IllegalArgumentException;
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new ExponentialBackoff(3), detector);

        try {
            retryPolicy.addRetryEventListener(null);
        } catch (Exception e) {
            Assert.assertTrue("null listener raises iae", e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testExponentialRetryPolicy() throws Exception {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return e instanceof IllegalArgumentException;
            }
        };
        RetryEventListener retryEventListener = new RetryEventListener() {
            @Override
            public void onRetry(RetryEvent evt) {
                retryCount = evt.getRetryCount();
                eventCount++;
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new ExponentialBackoff(3), detector);
        retryPolicy.addRetryEventListener(retryEventListener);
        boolean thrown = false;
        try {
            retryPolicy.action(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    throw new IllegalArgumentException();
                }
            });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                thrown = true;
            }
        }
        Assert.assertTrue(thrown);
        Assert.assertEquals("event count", 4, eventCount);
        Assert.assertEquals("retry count", 3, retryCount);
    }

    @Test
    public void testIncrementalRetryPolicy() throws Exception {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return e instanceof IllegalArgumentException;
            }
        };
        RetryEventListener retryEventListener = new RetryEventListener() {
            @Override
            public void onRetry(RetryEvent evt) {
                retryCount = evt.getRetryCount();
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new Incremental(3, 0, 100), detector);
        retryPolicy.addRetryEventListener(retryEventListener);
        boolean thrown = false;
        try {
            retryPolicy.action(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    throw new IllegalArgumentException();
                }
            });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                thrown = true;
            }
        }
        Assert.assertTrue(thrown);
        Assert.assertEquals("retry count", 3, retryCount);
    }

    @Test
    public void testFixedIntervalRetryPolicy() throws Exception {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return e instanceof IllegalArgumentException;
            }
        };
        RetryEventListener retryEventListener = new RetryEventListener() {
            @Override
            public void onRetry(RetryEvent evt) {
                retryCount = evt.getRetryCount();
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new FixedInterval(3, 100), detector);
        retryPolicy.addRetryEventListener(retryEventListener);
        boolean thrown = false;
        try {
            retryPolicy.action(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    throw new IllegalArgumentException();
                }
            });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                thrown = true;
            }
        }
        Assert.assertTrue(thrown);
        Assert.assertEquals("retry count", 3, retryCount);
    }

    @Before
    public void setRetryCount() {
        retryCount = 0;
    }

    @Test
    public void testNonTransientException() throws Exception {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return false;
            }
        };
        RetryEventListener retryEventListener = new RetryEventListener() {
            @Override
            public void onRetry(RetryEvent evt) {
                retryCount = evt.getRetryCount();
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new FixedInterval(1, 10), detector);
        retryPolicy.addRetryEventListener(retryEventListener);
        boolean thrown = false;
        try {
            retryPolicy.action(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    throw new IllegalArgumentException();
                }
            });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                thrown = true;
            }
        }
        Assert.assertTrue(thrown);
        Assert.assertEquals("retry count", 0, retryCount);
    }

    @Test
    public void testTransientExceptionAndReturn() throws Exception {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return true;
            }
        };
        RetryEventListener retryEventListener = new RetryEventListener() {
            @Override
            public void onRetry(RetryEvent evt) {
                Assert.assertTrue("cause is illegal argument", evt.getCause() instanceof IllegalArgumentException);
                retryCount = evt.getRetryCount();
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new FixedInterval(2, 10), detector);
        retryPolicy.addRetryEventListener(retryEventListener);
        boolean thrown = false;
        int result = 0;
        try {
            result = retryPolicy.action(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    if (retryCount == 1) {
                        return 5;
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            });
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                thrown = true;
            }
        }
        Assert.assertFalse("retry success, no throw", thrown);
        Assert.assertEquals("retry count", 1, retryCount);
        Assert.assertEquals("retry result", 5, result);
    }

    @Test
    public void testRetryPolicyReuse() throws Exception {
        TransientExceptionDetector detector = new TransientExceptionDetector() {
            @Override
            public boolean isTransient(Exception e) {
                return e instanceof IllegalArgumentException;
            }
        };
        RetryEventListener retryEventListener = new RetryEventListener() {
            @Override
            public void onRetry(RetryEvent evt) {
                retryCount = evt.getRetryCount();
                eventCount++;
            }
        };
        RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new FixedInterval(3, 100), detector);
        retryPolicy.addRetryEventListener(retryEventListener);

        for (int i = 0; i < 10; i++) {
            try {
                retryPolicy.action(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        throw new IllegalArgumentException();
                    }
                });
            } catch (Exception ignore) {
            }
        }
        Assert.assertEquals("event count", 40, eventCount);
        Assert.assertEquals("retry count", 3, retryCount);
    }
}
