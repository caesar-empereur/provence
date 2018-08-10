package com.hbase.pool.hikari;

/**
 * Created by yang on 2018/7/14.
 */
public interface ConnectionEntry {
    
    boolean compareAndSet(EntryState expectState, EntryState newState);
    
    void setState(EntryState newState);

    EntryState getState();

    void separate(ConnectionCloseCallback callback);
}
