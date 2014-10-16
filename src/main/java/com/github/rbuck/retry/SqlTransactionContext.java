package com.github.rbuck.retry;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The SQL transaction context for executed actions.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public interface SqlTransactionContext {

    /**
     * Get a SQL connection. The caller is responsible for calling Connection.close().
     * <p/>
     * This method is guaranteed to not return null.
     *
     * @return a SQL connection
     */
    Connection getConnection() throws SQLException;
}
