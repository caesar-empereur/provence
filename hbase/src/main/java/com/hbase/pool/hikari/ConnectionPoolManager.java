package com.hbase.pool.hikari;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.hbase.client.Connection;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/6.
 */
public class ConnectionPoolManager<E extends ConnectionEntry> {
    
    private final CopyOnWriteArrayList<E> sharedList;
    
    private final ThreadLocal<ArrayDeque<Object>> threadLocalList;
    
    private final SynchronousQueue<E> handOffQueue;
    
    private final AtomicInteger waiters;
    
    private static final long HAND_OFF_QUEUE_TIME_OUT = 10_000;
    
    private final ConcurrentMap<Connection, E> connectionContainer;
    
    public ConnectionPoolManager() {
        sharedList = new CopyOnWriteArrayList<>();
        threadLocalList = ThreadLocal.withInitial(() -> new ArrayDeque<>(16));
        handOffQueue = new SynchronousQueue<>(true);
        this.waiters = new AtomicInteger(0);
        connectionContainer = new ConcurrentHashMap<>();
    }
    
    @SuppressWarnings("all")
    public E borrow(long timeout,
                    final TimeUnit timeUnit,
                    EntryStateListener entryStateListener) throws InterruptedException {
        final ArrayDeque<Object> queue = threadLocalList.get();
        for (Object entry : queue) {
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
                    entryStateListener.addEntry(waiting - 1);
                }
                return entry;
            }
        }
        try {
            do {
                final long start = System.currentTimeMillis();
                E entry = handOffQueue.poll(timeout, TimeUnit.NANOSECONDS);
                if (entry != null || entry.compareAndSet(EntryState.NOT_IN_USE, EntryState.IN_USE)) {
                    return entry;
                }
                timeout -= System.currentTimeMillis() - start;
            }
            while (timeout > HAND_OFF_QUEUE_TIME_OUT);
            return null;
        }
        finally {
            waiters.decrementAndGet();
        }
    }
    
    public int getWaitingThreadCount() {
        return waiters.get();
    }
    
    public int getConnectionSize() {
        return sharedList.size();
    }
    
    public Boolean addEntry(E entry) {
        sharedList.add(entry);
        while (waiters.get() > 0 && !handOffQueue.offer(entry)) {
            Thread.yield();
        }
        return Boolean.TRUE;
    }
    
    public boolean remove(final E entry) {
        boolean removedEntry = sharedList.remove(entry);
        return removedEntry;
    }
    
    public void recycle(E entry) {
        entry.setState(EntryState.NOT_IN_USE);
        
        if (entry.getState() != EntryState.NOT_IN_USE || handOffQueue.offer(entry)) {
            return;
        }
        else {
            Thread.yield();
        }
        final ArrayDeque<Object> list = threadLocalList.get();
        list.add(new WeakReference<>(entry));
    }
}
