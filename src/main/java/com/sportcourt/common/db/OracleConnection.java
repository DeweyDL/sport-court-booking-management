package com.sportcourt.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class OracleConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleConnection.class);

    private static final String HOSTNAME;
    private static final String PORT;
    private static final String SERVICENAME;
    private static final String USERNAME;
    private static final String PASSWORD;
    private static final String URL;

    static {
        Properties properties = new Properties();

        try (InputStream input = OracleConnection.class.getClassLoader()
                .getResourceAsStream("db/db.properties.example")) {

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

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load database config: " + e.getMessage());
        }
    }

    private OracleConnection() {
    }

    public static Connection getOracleConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        LOGGER.debug("Opened Oracle database connection to {}", URL);
        return conn;
    }
}