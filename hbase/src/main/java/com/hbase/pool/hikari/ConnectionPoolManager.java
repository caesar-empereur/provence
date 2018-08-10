package com.hbase.pool.hikari;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/6.
 */
public class ConnectionPoolManager<E extends ConnectionEntry> {
    
    private final CopyOnWriteArrayList<E> sharedList;
    
    private final ThreadLocal<ArrayDeque<E>> threadLocalList;
    
    private final SynchronousQueue<E> handOffQueue;
    
    private final AtomicInteger waiters;

    private static final long HAND_OFF_QUEUE_TIME_OUT = 10_000;
    
    public ConnectionPoolManager() {
        sharedList = new CopyOnWriteArrayList<>();
        threadLocalList = ThreadLocal.withInitial(() -> new ArrayDeque<>(16));
        handOffQueue = new SynchronousQueue<>(true);
        this.waiters = new AtomicInteger(0);
    }
    
    public E borrow(long timeout, final TimeUnit timeUnit, EntryStateListener entryStateListener) {
        final ArrayDeque<E> queue = threadLocalList.get();
        for (E entry : queue) {
            final E weakReferenceEntry = ((WeakReference<E>) entry).get();
            if (weakReferenceEntry != null
                && weakReferenceEntry.compareAndSet(EntryState.NOT_IN_USE, EntryState.IN_USE)) {
                queue.poll();
                return weakReferenceEntry;
            }
        }
        int waiting = waiters.incrementAndGet();
        
        for (E entry : sharedList) {
            if (entry.compareAndSet(EntryState.NOT_IN_USE, EntryState.IN_USE)) {
                if (waiting > 1) {
                    entryStateListener.addConnectionEntry(waiting - 1);
                }
                return entry;
            }
        }
        try {
            do {
                final long start = System.currentTimeMillis();
                E entry = handOffQueue.poll(timeout, TimeUnit.NANOSECONDS);
                if (entry != null
                    || entry.compareAndSet(EntryState.NOT_IN_USE, EntryState.IN_USE)) {
                    return entry;
                }
                timeout -= System.currentTimeMillis() - start;
            }
            while (timeout > HAND_OFF_QUEUE_TIME_OUT);
            return null;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            waiters.decrementAndGet();
        }
        return null;
    }
}
