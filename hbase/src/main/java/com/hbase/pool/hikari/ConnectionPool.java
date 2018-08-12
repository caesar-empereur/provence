package com.hbase.pool.hikari;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import com.hbase.config.HbaseConfigProvider;
import com.hbase.pool.PoolAccessLock;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/8.
 */
public class ConnectionPool implements EntryStateListener {
    
    private Collection<Runnable> addConnectionQueue;
    
    private PoolAccessLock poolAccessLock;
    
    private ScheduledExecutorService houseKeepingExecutorService;
    
    private ExecutorService addConnectionExecutor;
    
    private ExecutorService closeConnectionExecutor;
    
    private ConnectionPoolManager manager;
    
    private Configuration configuration;
    
    private HbaseConfigProvider configProvider;
    
    public ConnectionPool(HbaseConfigProvider configProvider) {
        addConnectionQueue = Collections.unmodifiableCollection(new LinkedBlockingQueue<Runnable>());
        poolAccessLock = PoolAccessLock.SUSPEND_RESUME_LOCK;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
                                                                new ScheduledThreadPoolExecutor(1,
                                                                                                (Runnable r) -> new Thread(r,
                                                                                                                           "houseKeeping-thread"),
                                                                                                new ThreadPoolExecutor.DiscardPolicy());
        scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        houseKeepingExecutorService = scheduledThreadPoolExecutor;
        addConnectionExecutor = closeConnectionExecutor = Executors.newFixedThreadPool(1);
        manager = new ConnectionPoolManager();
        
        this.configuration = HBaseConfiguration.create();
        this.configuration.set(configProvider.getHbaseName(), configProvider.getHbaseValue());
        this.configProvider = configProvider;
        
        addEntryToManager();
        houseKeepingExecutorService.scheduleWithFixedDelay(this::fillPool, 10, 10, TimeUnit.SECONDS);
    }
    
    /**
     * ccreatePoolEntry
     * 
     * @return
     */
    private ConnectionEntry createConnectionEntry() {
        ConnectionEntry connectionEntry = new ConnectionEmbodiment(newConnection());
        houseKeepingExecutorService.schedule(() -> addEntry(manager.getWaitingThreadCount()), 10, TimeUnit.SECONDS);
        return connectionEntry;
    }
    
    /**
     * newPoolEntry
     * 
     * @return
     */
    private Connection newConnection() {
        try {
            return ConnectionFactory.createConnection(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private synchronized void fillPool() {
        int connectionsToAdd = configProvider.getMaxPoolSize() - manager.getConnectionSize();
        for (int i = 0; i < connectionsToAdd; i++) {
            addConnectionExecutor.submit(this::addEntryToManager);
        }
    }
    
    /**
     * addBagItem
     * 
     * @param waiting
     */
    @Override
    public void addEntry(final int waiting) {
        if (waiting > addConnectionQueue.size()) {
            addConnectionExecutor.submit(this::addEntryToManager);
        }
    }
    
    /**
     * POOL_ENTRY_CREATOR
     */
    @SuppressWarnings("all")
    private void addEntryToManager() {
        ConnectionEntry connectionEntry = createConnectionEntry();
        manager.addEntry(connectionEntry);
    }
    
    public Connection getConnection(final long timeout) throws TimeoutException {
        poolAccessLock.acquire();
        final long startTime = System.currentTimeMillis();
        
        try {
            long mutableTimeout = timeout;
            while (mutableTimeout > 0) {
                ConnectionEntry connectionEntry =
                                                manager.borrow(mutableTimeout,
                                                               TimeUnit.MILLISECONDS,
                                                               (int waiting) -> addEntry(manager.getWaitingThreadCount()
                                                                                         - 1));
                if (connectionEntry == null) {
                    break;
                }
                if (connectionEntry.getConnection().isClosed()) {
                    mutableTimeout = timeout - (System.currentTimeMillis() - startTime);
                }
                else {
                    return connectionEntry.getConnection();
                }
            }
            throw new TimeoutException("get connection timeout");
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimeoutException("get connection timeout");
        }
        finally {
            poolAccessLock.release();
        }
    }

    @SuppressWarnings("all")
    public void closeConnection(ConnectionEntry entry) {
        if (manager.remove(entry)) {
            Connection connection = entry.separate(() -> {
                manager.recycle(entry);
                closeConnectionExecutor.execute(this::fillPool);
            });
            try {
                connection.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
