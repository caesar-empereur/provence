package com.hbase.pool;

/**
 * Created by yang on 2018/7/14.
 */
public interface ConcurrentEntry {
    
    int STATE_NOT_IN_USE = 0;
    
    int STATE_IN_USE = 1;
    
    int STATE_REMOVED = -1;
    
    int STATE_RESERVED = -2;
    
    boolean compareAndSet(int expectState, int newState);
    
    void setState(int newState);
    
    int getState();
}
