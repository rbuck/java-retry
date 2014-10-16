package com.github.rbuck.retry;

import org.junit.Assert;
import org.junit.Test;

import java.sql.*;

/**
 * Tests the SqlTransientExceptionDetector class individually.
 *
 * @author Robert Buck (buck.robert.j@gmail.com)
 */
public class SqlTransientExceptionDetectorTest {
    @Test
    public void testRollback() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("rollback test", detector.isTransient(new SQLTransactionRollbackException()));
    }

    @Test
    public void testTransient() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("transient exception", detector.isTransient(new SQLTransientException()));
    }

    @Test
    public void testRecoverable() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("recoverable exception", detector.isTransient(new SQLRecoverableException()));
    }

    @Test
    public void testNonTransient() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertFalse("non-transient exception", detector.isTransient(new SQLNonTransientException("ignore", "nomatch")));
    }

    @Test
    public void testTransientConnection() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("transient connection test", detector.isTransient(new SQLTransientConnectionException("connection lost", "08nnn")));
    }

    @Test
    public void testTimeOutConnection() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("transient time out test", detector.isTransient(new SQLTimeoutException()));
    }

    @Test
    public void testLegacyConnection() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("legacy connection test", detector.isTransient(new SQLException("connection lost", "08nnn")));
    }

    @Test
    public void testLegacyRollback() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("legacy rollback test", detector.isTransient(new SQLException("rollback", "40nnn")));
    }

    @Test
    public void testLegacyDupeIndex() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("legacy dupe index test", detector.isTransient(new SQLException("duplicate value in unique index", "23nnn")));
    }

    @Test
    public void testLegacyDupeIndex2() {
        SqlTransientExceptionDetector detector = new SqlTransientExceptionDetector();
        Assert.assertTrue("legacy dupe index test", detector.isTransient(new SQLException("duplicate", "23505")));
    }
}
