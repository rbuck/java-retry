package com.github.rbuck.retry;

import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;

/**
 * Checks if the exception is a transient SQL exception.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class SqlTransientExceptionDetector implements TransientExceptionDetector {

    private final boolean treatDuplicatesAsTransient;

    public SqlTransientExceptionDetector() {
        this(true);
    }

    public SqlTransientExceptionDetector(boolean treatDuplicatesAsTransient) {
        this.treatDuplicatesAsTransient = treatDuplicatesAsTransient;
    }

    @Override
    public boolean isTransient(Exception e) {
        if (e instanceof SQLTransientException || e instanceof SQLRecoverableException) {
            return true;
        }
        if (e instanceof SQLNonTransientException) {
            return false;
        }
        if (e instanceof SQLException) {
            SQLException se = (SQLException) e;
            if (isSqlStateConnectionException(se) || isSqlStateRollbackException(se)) {
                return true;
            }
            if (isSqlStateDuplicateValueInUniqueIndex(se) && treatDuplicatesAsTransient) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the SQL exception a duplicate value in unique index.
     *
     * @param se the exception
     * @return true if it is a code 23505, otherwise false
     */
    public static boolean isSqlStateDuplicateValueInUniqueIndex(SQLException se) {
        String sqlState = se.getSQLState();
        return sqlState != null && (sqlState.equals("23505") || se.getMessage().contains("duplicate value in unique index"));
    }

    /**
     * Determines if the SQL exception is a connection exception
     *
     * @param se the exception
     * @return true if it is a code 08nnn, otherwise false
     */
    public static boolean isSqlStateConnectionException(SQLException se) {
        String sqlState = se.getSQLState();
        return sqlState != null && sqlState.startsWith("08");
    }

    /**
     * Determines if the SQL exception is a rollback exception
     *
     * @param se the exception
     * @return true if it is a code 40nnn, otherwise false
     */
    public static boolean isSqlStateRollbackException(SQLException se) {
        String sqlState = se.getSQLState();
        return sqlState != null && sqlState.startsWith("40");
    }

}
