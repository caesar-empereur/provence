package com.hbase.core;

import org.apache.hadoop.hbase.client.Connection;

import java.io.Serializable;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public final class DefaultSession extends DefaultTableManager implements Session<Serializable, Bytes> {
    
    private transient boolean closed;
    
    private transient boolean waitingForAutoClose;
    
    private transient Connection connection;

    private transient ConnectionProvider ConnectionProvider;
    
    @Override
    public Connection disconnect() {
        return connection = null;
    }
    
    @Override
    public Connection connection() {
        return ConnectionProvider.getConnection();
    }
    
    private void checkOpenOrWaitingForAutoClose() {
        if (!waitingForAutoClose && isClosed()) {
            throw new IllegalStateException("Session/EntityManager is closed");
        }
    }
    
    private boolean isClosed() {
        return closed;
    }
}
