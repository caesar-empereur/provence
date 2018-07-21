package com.hbase.pool;

import java.util.concurrent.Semaphore;

/**
 * Created by yang on 2018/7/14.
 */
public class PoolLock {
    
    public static final PoolLock SUSPEND_RESUME_LOCK = new PoolLock();
    
    /**
     * 并发的信号量中一共有 10000 个许可, 同一时间只有10000个线程能执行 acquire release 之间的代码,
     * 但不能保证线程安全
     */
    private static final int MAX_PERMITS = 10000;
    
    // 使用该类是为了限制线程并发的数量
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

    /**
     * 同一时间只有1个线程能执行acquire, release 之间的代码
     * 因此是暂停
     */
    public void suspend() {
        acquisitionSemaphore.acquireUninterruptibly(MAX_PERMITS);
    }
    
    public void resume() {
        acquisitionSemaphore.release(MAX_PERMITS);
    }
}
