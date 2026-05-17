package com.sportcourt.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public final class OracleConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleConnection.class);

    private static final int DEFAULT_POOL_MAX_SIZE = 10;
    private static final long DEFAULT_POOL_CONNECTION_TIMEOUT_MS = 10_000L;
    private static final int DEFAULT_POOL_VALIDATION_TIMEOUT_SECONDS = 3;

    private static final String HOSTNAME;
    private static final String PORT;
    private static final String SERVICENAME;
    private static final String USERNAME;
    private static final String PASSWORD;
    private static final String URL;
    private static final int POOL_MAX_SIZE;
    private static final long POOL_CONNECTION_TIMEOUT_MS;
    private static final int POOL_VALIDATION_TIMEOUT_SECONDS;
    private static final ConnectionPoolDataSource DATA_SOURCE;

    static {
        Properties properties = new Properties();

        try (InputStream input = OracleConnection.class.getClassLoader()
                .getResourceAsStream("db/db.properties")) {

            if (input == null) {
                throw new IllegalStateException("Cannot find db.properties in src/main/resources");
            }

            properties.load(input);

            HOSTNAME = properties.getProperty("db.host");
            PORT = properties.getProperty("db.port");
            SERVICENAME = properties.getProperty("db.service");
            USERNAME = properties.getProperty("db.username");
            PASSWORD = properties.getProperty("db.password");

            URL = "jdbc:oracle:thin:@//" + HOSTNAME + ":" + PORT + "/" + SERVICENAME;
            POOL_MAX_SIZE = intProperty(properties, "db.pool.maxSize", DEFAULT_POOL_MAX_SIZE, 1);
            POOL_CONNECTION_TIMEOUT_MS = longProperty(properties, "db.pool.connectionTimeoutMs",
                    DEFAULT_POOL_CONNECTION_TIMEOUT_MS, 1_000L);
            POOL_VALIDATION_TIMEOUT_SECONDS = intProperty(properties, "db.pool.validationTimeoutSeconds",
                    DEFAULT_POOL_VALIDATION_TIMEOUT_SECONDS, 1);
            DATA_SOURCE = createConnectionPoolDataSource();

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load database config: " + e.getMessage());
        } catch (SQLException e) {
            throw new ExceptionInInitializerError("Failed to initialize Oracle connection pool: " + e.getMessage());
        }
    }

    private OracleConnection() {
    }

    public static Connection getOracleConnection() throws SQLException {
        return ConnectionPool.getInstance().getConnection();
    }

    static PooledConnection openPooledConnection() throws SQLException {
        PooledConnection pooledConnection = DATA_SOURCE.getPooledConnection();
        LOGGER.debug("Opened Oracle pooled connection to {}", URL);
        return pooledConnection;
    }

    static int poolMaxSize() {
        return POOL_MAX_SIZE;
    }

    static long poolConnectionTimeoutMs() {
        return POOL_CONNECTION_TIMEOUT_MS;
    }

    static int poolValidationTimeoutSeconds() {
        return POOL_VALIDATION_TIMEOUT_SECONDS;
    }

    private static int intProperty(Properties properties, String key, int defaultValue, int minValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(minValue, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid integer config {}={}, using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private static long longProperty(Properties properties, String key, long defaultValue, long minValue) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(minValue, Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid long config {}={}, using default {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    private static ConnectionPoolDataSource createConnectionPoolDataSource() throws SQLException {
        oracle.jdbc.pool.OracleConnectionPoolDataSource dataSource =
                new oracle.jdbc.pool.OracleConnectionPoolDataSource();
        dataSource.setURL(URL);
        dataSource.setUser(USERNAME);
        dataSource.setPassword(PASSWORD);
        return dataSource;
    }
}
