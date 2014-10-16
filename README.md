# NuoDB Practices

[<img src="https://travis-ci.org/rbuck/enterprise-retry-4j.svg?branch=master" alt="Build Status" />](http://travis-ci.org/rbuck/enterprise-retry-4j)

Lets developers make their applications more resilient by adding robust
transient fault handling logic. Transient faults are errors that occur
because of some temporary condition such as network connectivity issues
or service unavailability. Typically, if you retry the operation that
resulted in a transient error a short time later, you find that the
error has disappeared.

## Retry

Infrastructure exists to facilitate automatic transaction retry, and exception
detectors are written to properly handle a variety of SQLException types, so that
retry occurs during transient exceptions, not others.

Retry has been implemented in a generic fashion so as to be pluggable in a number
of other non-SQL enterprise scenarios; so long as an exception detector is
appropriately written, business level activities can be written in a fault
tolerant manner.

### Generic Retry Example

Here is an example of your basic non-SQL retry loop:

```java
// the detector may optionally be a singleton...
TransientExceptionDetector detector = new TransientExceptionDetector() {
    @Override
    public boolean isTransient(Exception e) {
        // check exception type or content...
    }
};
// the retry policy and strategies are allocated per transaction
// and cannot be singletons, are typed...
RetryPolicy<Integer> retryPolicy = new RetryPolicy<>(new ExponentialBackoff(), detector);
int result = 0;
try {
    result = retryPolicy.action(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
            return 5;
        }
    });
} catch (Exception e) {
    // ... do something to handle or log ...
}
```

There are a few actors on stage here:

- the transient exception detector (returns true if the exception is a transient)
- the retry policy (consistently applied policy)
- the retry strategy (provides pluggable retry behaviors)
- the callable (the action to be executed with retry capabilities)

### SQL Retry Example

Here is an example of your basic SQL retry loop:

```java
SqlTransactionContext sqlTransactionContext = new ...
SqlRetryPolicy<Integer> sqlRetryPolicy = new SqlRetryPolicy<>(
    new FixedInterval(1, 100), sqlTransactionContext);
try {
    result = sqlRetryPolicy.action(new SqlCallable<Integer>() {
        @Override
        public Integer call(Connection connection) throws SQLException {
            int result = ... from SQL result set ...
            // critical: make sure to use try-with-resources to
            // properly close all statements and result sets! 
            return result;
        }
    });
} catch (Exception e) {
    // ... do something to handle or log ... 
}
```