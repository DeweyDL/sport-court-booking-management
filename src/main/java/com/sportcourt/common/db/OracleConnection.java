package com.sportcourt.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

public final class OracleConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(OracleConnection.class);
    private static final String PRIMARY_CONFIG = "db.properties";
    private static final String FALLBACK_CONFIG = "db.properties.example";
    private static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.OracleDriver";
    private static volatile boolean driverLoaded;

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
        if (driverLoaded) {
            return;
        }

        try {
            Class.forName(ORACLE_DRIVER_CLASS);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            if (registerDriverFromKnownLocations()) {
                driverLoaded = true;
                return;
            }

            throw new IllegalStateException(buildMissingDriverMessage(), e);
        }
    }

    private static boolean registerDriverFromKnownLocations() {
        List<File> candidates = new ArrayList<>();
        candidates.add(new File("lib", "ojdbc8.jar"));
        candidates.add(new File("lib", "ojdbc11.jar"));
        candidates.addAll(findLocalMavenDrivers("ojdbc8"));
        candidates.addAll(findLocalMavenDrivers("ojdbc11"));

        for (File candidate : candidates) {
            if (!candidate.isFile()) {
                continue;
            }

            try {
                registerDriverFromJar(candidate);
                LOGGER.info("Loaded Oracle JDBC driver from {}", candidate.getAbsolutePath());
                return true;
            } catch (ReflectiveOperationException | SQLException | MalformedURLException ex) {
                LOGGER.warn("Failed to load Oracle JDBC driver from {}", candidate.getAbsolutePath(), ex);
            }
        }

        return false;
    }

    private static List<File> findLocalMavenDrivers(String artifactId) {
        File artifactDir = new File(
                System.getProperty("user.home"),
                ".m2/repository/com/oracle/database/jdbc/" + artifactId
        );
        File[] versionDirs = artifactDir.listFiles(File::isDirectory);
        List<File> drivers = new ArrayList<>();

        if (versionDirs == null) {
            return drivers;
        }

        java.util.Arrays.sort(versionDirs, Comparator.comparing(File::getName).reversed());
        for (File versionDir : versionDirs) {
            File jar = new File(versionDir, artifactId + "-" + versionDir.getName() + ".jar");
            if (jar.isFile()) {
                drivers.add(jar);
            }
        }

        return drivers;
    }

    private static void registerDriverFromJar(File jarFile)
            throws MalformedURLException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, SQLException {
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[] { jarUrl }, OracleConnection.class.getClassLoader());
        Class<?> driverClass = Class.forName(ORACLE_DRIVER_CLASS, true, loader);
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
        DriverManager.registerDriver(new DriverShim(driver));
    }

    private static String buildMissingDriverMessage() {
        return "Oracle JDBC driver not found on the runtime classpath. "
                + "The dependency already exists in pom.xml, so run the app as a Maven project or add ojdbc to the runtime classpath. "
                + "Fallback locations checked: ./lib/ojdbc8.jar, ./lib/ojdbc11.jar, and the local Maven cache.";
    }

    private static final class DriverShim implements Driver {
        private final Driver delegate;

        private DriverShim(Driver delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return delegate.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegate.acceptsURL(url);
        }

        @Override
        public java.sql.DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return delegate.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegate.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegate.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegate.jdbcCompliant();
        }

        @Override
        public java.util.logging.Logger getParentLogger() throws java.sql.SQLFeatureNotSupportedException {
            return delegate.getParentLogger();
        }
    }
}
