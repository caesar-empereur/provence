package com.hbase.core;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Connection;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class PooledConnections {
    
    private static final Log log = LogFactory.getLog(PooledConnections.class);
    
    private ConnectionCreator connectionCreator;
    
    private final boolean autoCommit;
    
    private final int minSize, maxSize;
    
    private final ConcurrentLinkedQueue<Connection> allConnections = new ConcurrentLinkedQueue<>();
    
    private final ConcurrentLinkedQueue<Connection> availableConnections =
                                                                         new ConcurrentLinkedQueue<>();
    
    public Connection poll() {
        Connection conn = availableConnections.poll();
        if (conn == null) {
            synchronized (allConnections) {
                if (allConnections.size() < maxSize) {
                    addConnections(1);
                    return poll();
                }
            }
            throw new IllegalStateException("The internal connection pool has reached its maximum size and no connection is currently available!");
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
    
    public void add(Connection conn) {
        availableConnections.offer(conn);
    }
    
    public int size() {
        return availableConnections.size();
    }
    
    private PooledConnections(Builder builder) {
        connectionCreator = builder.connectionCreator;
        autoCommit = builder.autoCommit;
        maxSize = builder.maxSize;
        minSize = builder.minSize;
        addConnections(builder.initialSize);
    }
    
    protected void removeConnections(int numberToBeRemoved) {
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
    
    protected void addConnections(int numberOfConnections) {
        for (int i = 0; i < numberOfConnections; i++) {
            Connection connection = connectionCreator.createConnection();
            allConnections.add(connection);
            availableConnections.add(connection);
        }
    }
    
    static class Builder {
        
        private final ConnectionCreator connectionCreator;
        
        private final boolean autoCommit;
        
        private int initialSize = 1;
        
        private int minSize = 1;
        
        private int maxSize = 20;
        
        public Builder(ConnectionCreator connectionCreator, boolean autoCommit) {
            this.connectionCreator = connectionCreator;
            this.autoCommit = autoCommit;
        }
        
        public Builder initialSize(int initialSize) {
            this.initialSize = initialSize;
            return this;
        }
        
        public Builder minSize(int minSize) {
            this.minSize = minSize;
            return this;
        }
        
        public Builder maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }
        
        public PooledConnections build() {
            return new PooledConnections(this);
        }
    }
}
