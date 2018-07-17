package com.hbase.pool;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yang on 2018/7/17.
 */
public class ConnectionPoolEntry implements ConcurrentEntry {

    private static final AtomicInteger state;

    static {
        state = new AtomicInteger(0);
    }

    @Override
    public boolean compareAndSet(int expectState, int newState) {
        return state.compareAndSet(expectState,newState);
    }

    @Override
    public void setState(int newState) {
        state.set(newState);
    }

    @Override
    public int getState() {
        return state.get();
    }
}
