package com.hbase.pool.hibernate;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    
    private static final Log log = LogFactory.getLog(ConnectionPool.class);
    
    private int maxSize;
    
    private int minSize;
    
    private int initSize;
    
    private String quorum;
    
    private Configuration configuration = HBaseConfiguration.create();
    
    private final ConcurrentLinkedQueue<Connection> allConnections = new ConcurrentLinkedQueue<>();
    
    public ConnectionPool(int maxSize, int minSize, int initSize, String quorum) {
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.initSize = initSize;
        this.quorum = quorum;
    }
    
    public Connection poll() {
        Connection conn = allConnections.poll();
        if (conn == null) {
            synchronized (allConnections) {
                if (allConnections.size() < maxSize) {
                    addConnections(1);
                    return poll();
                }
            }
            throw new IllegalStateException("The connection pool has reached its maximum size and no connection is currently available!");
        }
        return conn;
    }
    
    public void validate() {
        log.info("当前连接数 " + size());
        final int size = size();
        if (size < minSize) {
            int numberToBeAdded = minSize - size;
            addConnections(numberToBeAdded);
        }
        else if (size > maxSize) {
            int numberToBeRemoved = size - maxSize;
            removeConnections(numberToBeRemoved);
        }
    }
    
    public int size() {
        return allConnections.size();
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
    }
    
    private void addConnections(int numberOfConnections) {
        for (int i = 0; i < numberOfConnections; i++) {
            Connection connection = createConnection();
            allConnections.offer(connection);
        }
    }
    
    private Connection createConnection() {
        if (configuration == null) {
            configuration = HBaseConfiguration.create();
            configuration.set(HConstants.ZOOKEEPER_QUORUM, quorum);
        }
        try {
            return ConnectionFactory.createConnection(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
