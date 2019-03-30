package com.hbase.pool.hibernate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Connection;

import com.hbase.pool.ConnectionProvider;
import com.hbase.spring.CustomEnvironmentListener;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionPoolManager implements ConnectionProvider<Connection> {

    private static ConnectionPoolManager instance;

    private static final String WINDOWS_SYSTEM = "Windows";

    private static final Log log = LogFactory.getLog(ConnectionPoolManager.class);

    private ConnectionPool connectionPool;

    private ConnectionPoolManager() {
        connectionPool = ConnectionPool.getInstance(CustomEnvironmentListener.connectionConfig);
        final long validationInterval = CustomEnvironmentListener.connectionConfig.getValidateInterval();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(connectionPool::validate, 0, validationInterval, TimeUnit.SECONDS);
    }
    
    @Override
    public Connection getConnection() {
        return connectionPool.poll(() -> connectionPool.addConnections(1));
    }
    
    @Override
    public void recycleConnection(Connection connection) {
        connectionPool.recycle(connection);
        log.info("回收连接");
    }

    public static ConnectionPoolManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionPoolManager.class) {
                if (instance == null) {
                    instance = new ConnectionPoolManager();
                }
            }
        }
        return instance;
    }

    private Object readResolve() {
        return instance;
    }
}
