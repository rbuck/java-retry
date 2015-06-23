package com.github.rbuck.retry;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

/**
 * Tests the SqlRetryPolicy class.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class SqlRetryPolicyTest {

    private void internalTest(final MockConnection.ExceptionType exceptionType) {
        SqlTransactionContext sqlTransactionContext = new SqlTransactionContext() {

            private Connection connection;

            @Override
            public Connection getConnection() throws SQLException {
                if (connection == null) {
                    connection = new MockConnection(exceptionType);
                }
                return connection;
            }
        };
        int result = 0;
        SqlRetryPolicy<Integer> sqlRetryPolicy = new SqlRetryPolicy<>(new FixedInterval(1, 100), sqlTransactionContext);
        try {
            result = sqlRetryPolicy.action(new SqlCallable<Integer>() {
                @Override
                public Integer call(Connection connection) throws SQLException {
                    return 5;
                }
            });
        } catch (Exception e) {
            if (exceptionType == MockConnection.ExceptionType.ConnectionCreateFail) {
                Assert.assertTrue("is non-transient connection exception", e instanceof SQLNonTransientConnectionException);
            }
        }
        if (exceptionType == MockConnection.ExceptionType.Nothing) {
            Assert.assertEquals("no exception", 5, result);
        }
        if (exceptionType == MockConnection.ExceptionType.Rollback) {
            try {
                Assert.assertTrue("rollback", ((MockConnection) sqlTransactionContext.getConnection()).isRollbackCalled());
            } catch (SQLException ignore) {
                Assert.assertTrue("should not get here", false);
            }
        } else {
            try {
                Assert.assertFalse("no rollback", ((MockConnection) sqlTransactionContext.getConnection()).isRollbackCalled());
            } catch (SQLException ignore) {
                if (exceptionType != MockConnection.ExceptionType.ConnectionCreateFail && exceptionType != MockConnection.ExceptionType.Interrupted) {
                    Assert.assertTrue("should not get here", false);
                }
            }
        }

    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        protected void starting(Description description) {
            System.out.println("Starting test: " + description.getMethodName());
        }
    };

    @Test
    public void testSqlCreateConnectionFailure() {
        internalTest(MockConnection.ExceptionType.ConnectionCreateFail);
    }

    @Test
    public void testNoException() {
        internalTest(MockConnection.ExceptionType.Nothing);
    }

    @Test
    public void testRollback() {
        internalTest(MockConnection.ExceptionType.Rollback);
    }

    @Test
    public void testStaleConnection() {
        internalTest(MockConnection.ExceptionType.StaleConnection);
    }

    @Test
    public void testInterruptedException() {
        internalTest(MockConnection.ExceptionType.Interrupted);
    }
}
