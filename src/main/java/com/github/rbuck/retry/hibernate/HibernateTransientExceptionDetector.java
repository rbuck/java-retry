package com.github.rbuck.retry.hibernate;

import com.github.rbuck.retry.SqlTransientExceptionDetector;
import org.hibernate.JDBCException;
import org.hibernate.PessimisticLockException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;

/**
 * Checks if the Hibernate exception is a transient SQL exception.
 * This class always treats the following exception types as transient:
 * <ul>
 * <li>LockAcquisitionException</li>
 * <li>PessimisticLockException</li>
 * <li>JDBCConnectionException</li>
 * </ul>
 * <p/>
 * The following types wrap SQLException and delegates detection of transients
 * to the super class, SqlTransientExceptionDetector:
 * <ul>
 * <li>GenericJDBCException</li>
 * </ul>
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class HibernateTransientExceptionDetector extends SqlTransientExceptionDetector {

    public HibernateTransientExceptionDetector() {
    }

    @Override
    public boolean isTransient(Exception e) {
        if (e instanceof LockAcquisitionException || e instanceof PessimisticLockException || e instanceof JDBCConnectionException) {
            return true;
        }
        if (e instanceof GenericJDBCException) {
            JDBCException se = (JDBCException) e;
            return super.isTransient(se.getSQLException());
        }
        return false;
    }
}
