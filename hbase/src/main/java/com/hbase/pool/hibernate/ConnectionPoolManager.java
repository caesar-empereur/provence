package com.hbase.pool.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Connection;

import com.hbase.config.ConnectionConfig;
import com.hbase.pool.ConnectionProvider;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionPoolManager implements ConnectionProvider<Connection> {

    private static ConnectionPoolManager instance;

    private static final String WINDOWS_SYSTEM = "Windows";

    private static final Log log = LogFactory.getLog(ConnectionPoolManager.class);

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
            checkHadoopDir(properties);
        }
        catch (IOException e) {
            throw new RuntimeException("配置文件出错");
        }
    }

    /**
     * 处理 windows 系统在调试本项目时 hadoop 环境的问题
     */
    private static void checkHadoopDir(Properties properties) {
        Properties props = System.getProperties();
        String osName = props.getProperty("os.name");
        if (StringUtils.containsIgnoreCase(osName, WINDOWS_SYSTEM)) {
            connectionConfig.setHadoopDir(properties.getProperty("hadoopDir"));
        }
    }

    private ConnectionPoolManager() {
        connectionPool = ConnectionPool.getInstance(connectionConfig);
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
