package com.hbase.pool.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hbase.config.ConnectionConfig;
import org.apache.hadoop.hbase.client.Connection;

import com.hbase.pool.ConnectionProvider;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionPoolManager implements ConnectionProvider {
    
    private ConnectionPool connectionPool;
    
    private static ConnectionConfig connectionConfig;
    
    private static String quorum;
    
    static {
        InputStream inputStream =
                                ConnectionPoolManager.class.getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            connectionConfig = new ConnectionConfig(Integer.valueOf(properties.getProperty("initSize")),
                                                  Integer.valueOf(properties.getProperty("minSize")),
                                                  Integer.valueOf(properties.getProperty("maxSize")),
                                                  Integer.valueOf(properties.getProperty("validateInterval")));
            quorum = properties.getProperty("quorum");
        }
        catch (IOException e) {
            throw new RuntimeException("配置文件出错");
        }
    }
    
    public ConnectionPoolManager() {
        connectionPool = new ConnectionPool(connectionConfig.getMaxSize(),
                                            connectionConfig.getMinSize(),
                                            connectionConfig.getInitSize(),
                                            quorum);
        
        final long validationInterval = connectionConfig.getValidateInterval().longValue();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(connectionPool::validate, 0, validationInterval, TimeUnit.SECONDS);
    }
    
    @Override
    public Connection getConnection() {
        return connectionPool.poll(() -> connectionPool.addConnections(1));
    }
    
    @Override
    public void releaseConnection(Connection connection) {
        connectionPool.recycle(connection);
    }
}
