package com.github.rbuck.retry;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A SAM type permitting the use of lambda expressions whose default action is
 * to perform a SQL transaction using the given connection.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public interface SqlCallable<V> {

    /**
     * Implementation for the action to perform.
     *
     * @param connection the connection against which to perform the action
     * @return the result of the transaction
     * @throws java.sql.SQLException throw with detail related to cause; callers should
     *                               retry if the exception type is
     *                               {@link java.sql.SQLTransientException}.
     */
    V call(Connection connection) throws SQLException;

}
