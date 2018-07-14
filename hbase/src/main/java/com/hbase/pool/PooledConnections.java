package com.hbase.pool;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.hbase.config.HbaseConfigProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class PooledConnections implements InitializingBean {
    
    private static final Log log = LogFactory.getLog(PooledConnections.class);
    
    private final int minSize, maxSize;
    
    @Resource
    private HbaseConfigProvider hbaseConfig;

    private Configuration configuration = HBaseConfiguration.create();
    
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

    public void validate() {
        final int size = size();
        if (size >= minSize ) {
            log.debug( "Connection pool now considered primed; min-size will be maintained" );
        }
        if ( size < minSize) {
            int numberToBeAdded = minSize - size;
            addConnections( numberToBeAdded );
        }
        else if ( size > maxSize ) {
            int numberToBeRemoved = size - maxSize;
            removeConnections( numberToBeRemoved );
        }
    }

    public int size() {
        return availableConnections.size();
    }

    public void add(Connection conn) {
        availableConnections.offer(conn);
    }

    private PooledConnections(Builder builder) {
        maxSize = builder.maxSize;
        minSize = builder.minSize;
        addConnections(builder.initialSize);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        configuration.set(hbaseConfig.getHbaseName(), hbaseConfig.getHbaseValue());
    }

    private void addConnections(int numberOfConnections) {
        for (int i = 0; i < numberOfConnections; i++) {
            Connection connection = buildConnection();
            allConnections.add(connection);
            availableConnections.add(connection);
        }
    }

    private Connection buildConnection() {
        try {
            return ConnectionFactory.createConnection(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    static class Builder {
        
        private int initialSize = 1;
        
        private int minSize = 1;
        
        private int maxSize = 20;

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
