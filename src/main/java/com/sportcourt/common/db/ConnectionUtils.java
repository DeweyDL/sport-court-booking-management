package com.sportcourt.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ConnectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionUtils.class);

    private ConnectionUtils() {
    }

    public static Connection getMyConnection() throws SQLException {
        return OracleConnection.getOracleConnection();
    }

    public static void shutdownConnectionPool() {
        ConnectionPool.getInstance().shutdown();
    }

    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not close ResultSet", e);
        }

        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not close PreparedStatement", e);
        }

        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.warn("Could not close Connection", e);
        }
    }
}
