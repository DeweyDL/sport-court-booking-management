package com.sportcourt.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

final class ConnectionPool {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPool.class);
    private static final ConnectionPool INSTANCE = new ConnectionPool();

    private final Deque<PooledConnection> idleConnections = new ArrayDeque<>();
    private final Set<PooledConnection> closingConnections =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private final int maxPoolSize = OracleConnection.poolMaxSize();
    private final long connectionTimeoutMs = OracleConnection.poolConnectionTimeoutMs();
    private final int validationTimeoutSeconds = OracleConnection.poolValidationTimeoutSeconds();

    private int totalConnections;
    private boolean shutdown;

    private ConnectionPool() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown, "sportcourt-db-pool-shutdown"));
    }

    static ConnectionPool getInstance() {
        return INSTANCE;
    }

    Connection getConnection() throws SQLException {
        long deadline = System.currentTimeMillis() + connectionTimeoutMs;

        while (true) {
            boolean shouldCreateConnection = false;

            synchronized (this) {
                ensureRunning();

                PooledConnection idleConnection = borrowIdleConnection();
                if (idleConnection != null) {
                    try {
                        Connection connection = borrowLogicalConnection(idleConnection);
                        LOGGER.debug("Borrowed pooled Oracle connection. active={}, idle={}",
                                activeConnectionCount(), idleConnections.size());
                        return connection;
                    } catch (SQLException e) {
                        totalConnections--;
                        closePooledConnection(idleConnection);
                        notifyAll();
                        LOGGER.warn("Discarding invalid idle Oracle pooled connection", e);
                        continue;
                    }
                }

                if (totalConnections < maxPoolSize) {
                    totalConnections++;
                    shouldCreateConnection = true;
                } else {
                    long waitTime = deadline - System.currentTimeMillis();
                    if (waitTime <= 0) {
                        throw new SQLException("Timed out waiting for an Oracle connection from the pool.");
                    }
                    waitForConnection(waitTime);
                }
            }

            if (shouldCreateConnection) {
                try {
                    PooledConnection pooledConnection = OracleConnection.openPooledConnection();
                    pooledConnection.addConnectionEventListener(new PoolConnectionListener(this, pooledConnection));
                    LOGGER.debug("Created pooled Oracle connection. active={}, idle={}",
                            activeConnectionCount(), idleConnections.size());
                    return borrowLogicalConnection(pooledConnection);
                } catch (SQLException e) {
                    synchronized (this) {
                        totalConnections--;
                        notifyAll();
                    }
                    throw e;
                }
            }
        }
    }

    private void waitForConnection(long waitTimeMs) throws SQLException {
        try {
            wait(waitTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for an Oracle connection from the pool.", e);
        }
    }

    private PooledConnection borrowIdleConnection() {
        return idleConnections.isEmpty() ? null : idleConnections.removeFirst();
    }

    private Connection borrowLogicalConnection(PooledConnection pooledConnection) throws SQLException {
        Connection connection = pooledConnection.getConnection();
        if (connection == null || connection.isClosed() || !connection.isValid(validationTimeoutSeconds)) {
            throw new SQLException("Oracle pooled connection is not valid.");
        }
        return connection;
    }

    private void release(PooledConnection pooledConnection) {
        synchronized (this) {
            if (closingConnections.remove(pooledConnection)) {
                notifyAll();
                return;
            }
            if (shutdown) {
                totalConnections--;
                closePooledConnection(pooledConnection);
            } else {
                idleConnections.addLast(pooledConnection);
            }
            notifyAll();
            LOGGER.debug("Released Oracle connection. active={}, idle={}",
                    activeConnectionCount(), idleConnections.size());
        }
    }

    private void discard(PooledConnection pooledConnection) {
        synchronized (this) {
            if (closingConnections.remove(pooledConnection)) {
                notifyAll();
                return;
            }
            totalConnections--;
            closePooledConnection(pooledConnection);
            notifyAll();
            LOGGER.debug("Discarded Oracle pooled connection. active={}, idle={}",
                    activeConnectionCount(), idleConnections.size());
        }
    }

    private int activeConnectionCount() {
        return totalConnections - idleConnections.size();
    }

    synchronized void shutdown() {
        if (shutdown) {
            return;
        }
        shutdown = true;
        while (!idleConnections.isEmpty()) {
            closePooledConnection(idleConnections.removeFirst());
            totalConnections--;
        }
        notifyAll();
    }

    private void ensureRunning() throws SQLException {
        if (shutdown) {
            throw new SQLException("Oracle connection pool has been shut down.");
        }
    }

    private void closePooledConnection(PooledConnection pooledConnection) {
        if (pooledConnection == null) {
            return;
        }
        try {
            synchronized (this) {
                closingConnections.add(pooledConnection);
            }
            pooledConnection.close();
        } catch (SQLException e) {
            LOGGER.warn("Could not close Oracle pooled connection", e);
        }
    }

    private static final class PoolConnectionListener implements javax.sql.ConnectionEventListener {
        private final ConnectionPool pool;
        private final PooledConnection pooledConnection;

        private PoolConnectionListener(ConnectionPool pool, PooledConnection pooledConnection) {
            this.pool = pool;
            this.pooledConnection = pooledConnection;
        }

        @Override
        public void connectionClosed(javax.sql.ConnectionEvent event) {
            pool.release(pooledConnection);
        }

        @Override
        public void connectionErrorOccurred(javax.sql.ConnectionEvent event) {
            pool.discard(pooledConnection);
        }
    }
}
