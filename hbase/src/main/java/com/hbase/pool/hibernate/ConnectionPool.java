package com.hbase.pool.hibernate;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    
    private final ConcurrentLinkedQueue<Connection> availableConnections =
                                                                         new ConcurrentLinkedQueue<>();
    
    public ConnectionPool(int maxSize, int minSize, int initSize, int interval, String quorum) {
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.initSize = initSize;
        this.quorum = quorum;
        
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::validate, 0, interval, TimeUnit.SECONDS);
    }
    
    public Connection poll() {
        Connection conn = availableConnections.poll();
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
    
    public void close() {
        try {
            int allocationCount = allConnections.size() - availableConnections.size();
            if (allocationCount > 0) {
                log.error("Connection leak detected: there are " + allocationCount
                          + " unclosed connections upon shutting down pool ");
            }
        }
        finally {
            for (Connection connection : allConnections) {
                try {
                    connection.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void validate() {
        log.info("当前连接数 " + size());
        final int size = size();
        if (size >= minSize) {
            log.debug("Connection pool now considered primed; min-size will be maintained");
        }
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
        return availableConnections.size();
    }
    
    public void add(Connection conn) {
        availableConnections.offer(conn);
    }
    
    private void removeConnections(int numberToBeRemoved) {
        for (int i = 0; i < numberToBeRemoved; i++) {
            Connection connection = availableConnections.poll();
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
            allConnections.add(connection);
            availableConnections.add(connection);
        }
    }
    
    private Connection createConnection() {
        if (configuration == null) {
//            System.setProperty("hadoop.home.dir", "D:\\dev\\app\\hadoop-common\\hadoop-common-2.2.0-bin-master");
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
