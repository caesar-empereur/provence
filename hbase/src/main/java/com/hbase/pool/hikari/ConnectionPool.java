package com.hbase.pool.hikari;

import com.hbase.config.HbaseConfigProvider;
import com.hbase.pool.PoolAccessLock;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

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
    
    private ConnectionPoolManager manager;
    
    private Configuration configuration;
    
    private HbaseConfigProvider configProvider;
    
    public ConnectionPool(HbaseConfigProvider configProvider) {
        addConnectionQueue =
                           Collections.unmodifiableCollection(new LinkedBlockingQueue<Runnable>());
        poolAccessLock = PoolAccessLock.SUSPEND_RESUME_LOCK;
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
                                                                new ScheduledThreadPoolExecutor(1,
                                                                                                (Runnable r) -> new Thread(r,
                                                                                                                           "houseKeeping-thread"),
                                                                                                new ThreadPoolExecutor.DiscardPolicy());
        scheduledThreadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        houseKeepingExecutorService = scheduledThreadPoolExecutor;
        addConnectionExecutor = Executors.newFixedThreadPool(1);
        manager = new ConnectionPoolManager();
        
        this.configuration = HBaseConfiguration.create();
        this.configuration.set(configProvider.getHbaseName(), configProvider.getHbaseValue());
        this.configProvider = configProvider;
        addEntryToManager();
    }
    
    private ConnectionEntry createConnectionEntry() {
        ConnectionEntry connectionEntry = new ConnectionEntryImpl(buildConnection());
        houseKeepingExecutorService.schedule(() -> addEntry(manager.getWaitingThreadCount()),
                                             10,
                                             TimeUnit.SECONDS);
        return connectionEntry;
    }
    
    private Connection buildConnection() {
        try {
            return ConnectionFactory.createConnection(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private synchronized void fillPool() {
        int connectionsToAdd = configProvider.getMaxPoolSize()-manager.getConnectionSize();
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
    private void addEntryToManager() {
        ConnectionEntry connectionEntry = createConnectionEntry();
        manager.addEntry(connectionEntry);
    }
    
}
