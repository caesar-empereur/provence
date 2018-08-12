package com.hbase.pool.hikari;

import org.apache.hadoop.hbase.client.Connection;

/**
 * Created by yang on 2018/7/14.
 */
public interface ConnectionEntry {
    
    boolean compareAndSet(EntryState expectState, EntryState newState);
    
    void setState(EntryState newState);

    EntryState getState();

    Connection separate(ConnectionCloseCallback callback);

    Connection getConnection();
}
