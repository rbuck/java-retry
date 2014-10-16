package com.github.rbuck.retry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * A retry policy for SQL operations.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class SqlRetryPolicy<V> {

    private final SqlTransactionContext sqlTransactionContext;
    private final RetryPolicy<V> retryPolicy;

    /**
     * Implements a retry policy using the specified strategy and transient error detection algorithm.
     *
     * @param retryStrategy the strategy that implements retry
     */
    public SqlRetryPolicy(RetryStrategy retryStrategy, SqlTransactionContext sqlTransactionContext) {
        this(retryStrategy, new SqlTransientExceptionDetector(), sqlTransactionContext);
    }

    /**
     * Implements a retry policy using the specified strategy and transient error detection algorithm.
     *
     * @param retryStrategy              the strategy that implements retry
     * @param transientExceptionDetector the transient error detection algorithm
     */
    public SqlRetryPolicy(RetryStrategy retryStrategy, TransientExceptionDetector transientExceptionDetector, SqlTransactionContext sqlTransactionContext) {
        this.retryPolicy = new RetryPolicy<>(retryStrategy, transientExceptionDetector);
        this.sqlTransactionContext = sqlTransactionContext;
    }

    public V action(final SqlCallable<V> callable) throws Exception {
        return retryPolicy.action(new Callable<V>() {
            @Override
            public V call() throws Exception {
                try (Connection connection = sqlTransactionContext.getConnection()) {
                    try {
                        V value = callable.call(connection);
                        connection.commit();
                        return value;
                    } catch (SQLException se) {
                        if (!SqlTransientExceptionDetector.isSqlStateConnectionException(se)) {
                            try {
                                connection.rollback();
                            } catch (SQLException ignored) {
                            }
                        }
                        throw se;
                    }
                }
            }
        });
    }
}
