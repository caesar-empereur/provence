package com.hbase.pool.hikari;

import org.apache.hadoop.hbase.client.Connection;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/6.
 */
public class ConnectionEntryImpl implements ConnectionEntry {
    
    private volatile EntryState state;

    private Connection connection;
    
    private static final AtomicReferenceFieldUpdater<ConnectionEntryImpl, EntryState> STATE_UPDATER;
    
    static {
        STATE_UPDATER = AtomicReferenceFieldUpdater.newUpdater(ConnectionEntryImpl.class,
                                                               EntryState.class,
                                                               "state");
    }

    public ConnectionEntryImpl(Connection connection) {
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
    public void separate(ConnectionCloseCallback callback) {
        try {
            connection.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        callback.onClose();
    }
}
