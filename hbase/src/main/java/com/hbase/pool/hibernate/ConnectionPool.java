package com.hbase.pool.hibernate;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hbase.config.ConnectionConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionPool {
    
    private static ConnectionPool instance;
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private int maxSize;
    
    private int minSize;
    
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private Configuration configuration;
    
    private final ConcurrentLinkedQueue<Connection> allConnections = new ConcurrentLinkedQueue<>();
    
    private ConnectionPool(ConnectionConfig connectionConfig) {

        log.info("开始实例化连接池");
        this.maxSize = connectionConfig.getMaxSize();
        this.minSize = connectionConfig.getMinSize();
        
        if (configuration == null) {
            System.setProperty("hadoop.home.dir", connectionConfig.getHadoopDir());
            configuration = HBaseConfiguration.create();
            configuration.set(HConstants.ZOOKEEPER_QUORUM, connectionConfig.getQuorum());
        }
    }
    
    public Connection poll(ObtainConnectionCallback callback) {
        Connection conn = allConnections.poll();
        if (conn == null || conn.isClosed()) {
            synchronized (allConnections) {
                return createConnection();
            }
        }
        executorService.submit(callback::onCreateConnection);
        return conn;
    }
    
    public void validate() {
        log.info("当前连接数 " + allConnections.size());
        final int size = allConnections.size();
        if (size < minSize) {
            int numberToBeAdded = minSize - size;
            addConnections(numberToBeAdded);
        }
        else if (size > maxSize) {
            int numberToBeRemoved = size - maxSize;
            removeConnections(numberToBeRemoved);
        }
    }
    
    private void removeConnections(int numberToBeRemoved) {
        for (int i = 0; i < numberToBeRemoved; i++) {
            Connection connection = allConnections.poll();
            if (connection != null) {
                try {
                    connection.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            allConnections.remove(connection);
        }
        log.info("移除连接数: " + numberToBeRemoved);
    }
    
    public void addConnections(int numberOfConnections) {
        for (int i = 0; i < numberOfConnections; i++) {
            Connection connection = createConnection();
            allConnections.offer(connection);
        }
        log.info("增加连接数：" + numberOfConnections);
    }
    
    private Connection createConnection() {
        log.info("创建连接");
        try {
            return ConnectionFactory.createConnection(configuration);
        }
        catch (IOException e) {
            log.error(e);
            throw new RuntimeException("创建连接失败，检查配置");
        }
    }
    
    public void recycle(Connection connection) {
        if (connection.isClosed()) {
            return;
        }
        allConnections.offer(connection);
    }
    
    public static ConnectionPool getInstance(ConnectionConfig connectionConfig) {
        if (instance == null) {
            synchronized (ConnectionPool.class) {
                if (instance == null) {
                    instance = new ConnectionPool(connectionConfig);
                }
            }
        }
        return instance;
    }
}
