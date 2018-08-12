package com.hbase.pool.hikari;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.apache.hadoop.hbase.client.Connection;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/6.
 */
public class ConnectionEmbodiment implements ConnectionEntry {
    
    private volatile EntryState state;
    
    private Connection connection;

    private volatile ScheduledFuture<?> endOfLife;

    private static final AtomicReferenceFieldUpdater<ConnectionEmbodiment, EntryState> STATE_UPDATER;
    
    static {
        STATE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ConnectionEmbodiment.class, EntryState.class, "state");
    }
    
    @Override
    public Connection getConnection() {
        return connection;
    }
    
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public ConnectionEmbodiment(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public boolean compareAndSet(EntryState expectState, EntryState newState) {
        return STATE_UPDATER.compareAndSet(this, expectState, newState);
    }
    
    @Override
    public void setState(EntryState newState) {
        STATE_UPDATER.set(this, newState);
    }
    
    @Override
    public EntryState getState() {
        return STATE_UPDATER.get(this);
    }
    
    @Override
    public Connection separate(ConnectionCloseCallback callback) {
        endOfLife = null;
        callback.onClose();
        return connection;
    }
}
