package com.github.rbuck.retry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A basic transaction context wrapping a java.sql.DataSource.
 */
public class BasicSqlTransactionContext implements SqlTransactionContext {

    private final DataSource dataSource;

    /**
     * Constructs a transaction context bean wrapping a SQL data source.
     *
     * @param dataSource the data source to use
     */
    public BasicSqlTransactionContext(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
