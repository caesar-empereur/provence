package com.hbase.pool;

import java.util.concurrent.Semaphore;

/**
 * Created by yang on 2018/7/14.
 */
public class PoolLock {
    
    public static final PoolLock SUSPEND_RESUME_LOCK = new PoolLock();
    
    private static final int MAX_PERMITS = 10000;
    
    private final Semaphore acquisitionSemaphore;
    
    /**
     * Default constructor
     */
    private PoolLock() {
        this(true);
    }
    
    private PoolLock(final boolean createSemaphore) {
        acquisitionSemaphore = (createSemaphore ? new Semaphore(MAX_PERMITS, true) : null);
    }
    
    public void acquire() {
        acquisitionSemaphore.acquireUninterruptibly();
    }
    
    public void release() {
        acquisitionSemaphore.release();
    }
    
    public void suspend() {
        acquisitionSemaphore.acquireUninterruptibly(MAX_PERMITS);
    }
    
    public void resume() {
        acquisitionSemaphore.release(MAX_PERMITS);
    }
}
