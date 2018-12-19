package com.hbase.pool.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.client.Connection;

import com.hbase.config.ConnectionConfig;
import com.hbase.pool.ConnectionProvider;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionPoolManager implements ConnectionProvider {
    
    private ConnectionPool connectionPool;
    
    private static ConnectionConfig connectionConfig;
    
    static {
        InputStream inputStream = ConnectionPoolManager.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            connectionConfig = new ConnectionConfig();
            connectionConfig.setInitSize(Integer.valueOf(properties.getProperty("initSize")));
            connectionConfig.setMinSize(Integer.valueOf(properties.getProperty("minSize")));
            connectionConfig.setMaxSize(Integer.valueOf(properties.getProperty("maxSize")));
            connectionConfig.setValidateInterval(Integer.valueOf(properties.getProperty("validateInterval")));
            connectionConfig.setQuorum(properties.getProperty("quorum"));
        }
        catch (IOException e) {
            throw new RuntimeException("配置文件出错");
        }
    }
    
    public ConnectionPoolManager() {
        connectionPool = new ConnectionPool(connectionConfig);
        final long validationInterval = connectionConfig.getValidateInterval().longValue();
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
    }
}
