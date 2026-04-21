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
    private static final String PRIMARY_CONFIG = "db.properties";
    private static final String FALLBACK_CONFIG = "db.properties.example";

    private static final String HOSTNAME;
    private static final String PORT;
    private static final String SERVICENAME;
    private static final String USERNAME;
    private static final String PASSWORD;
    private static final String URL;

    static {
        Properties properties = new Properties();

        try (InputStream input = openConfigStream()) {
            properties.load(input);

            HOSTNAME = requireProperty(properties, "db.host");
            PORT = requireProperty(properties, "db.port");
            SERVICENAME = requireProperty(properties, "db.service");
            USERNAME = requireProperty(properties, "db.username");
            PASSWORD = requireProperty(properties, "db.password");

            URL = "jdbc:oracle:thin:@//" + HOSTNAME + ":" + PORT + "/" + SERVICENAME;

        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load database config: " + e.getMessage());
        } catch (IllegalStateException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        }
    }

    private OracleConnection() {
    }

    public static Connection getOracleConnection() throws SQLException {
        loadOracleDriver();
        Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        LOGGER.debug("Opened Oracle database connection to {}", URL);
        return conn;
    }

    private static InputStream openConfigStream() {
        ClassLoader classLoader = OracleConnection.class.getClassLoader();
        InputStream input = classLoader.getResourceAsStream(PRIMARY_CONFIG);

        if (input != null) {
            return input;
        }

        input = classLoader.getResourceAsStream(FALLBACK_CONFIG);
        if (input != null) {
            LOGGER.warn("Using {} because {} was not found", FALLBACK_CONFIG, PRIMARY_CONFIG);
            return input;
        }

        throw new IllegalStateException(
                "Cannot find " + PRIMARY_CONFIG + " or " + FALLBACK_CONFIG + " in src/main/resources"
        );
    }

    private static String requireProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required database property: " + key);
        }

        if (value.startsWith("your_")) {
            throw new IllegalStateException(
                    "Database property " + key + " is still using the example placeholder value"
            );
        }

        return value.trim();
    }

    private static void loadOracleDriver() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Oracle JDBC driver not found. Add dependency com.oracle.database.jdbc:ojdbc11 to pom.xml",
                    e
            );
        }
    }
}
