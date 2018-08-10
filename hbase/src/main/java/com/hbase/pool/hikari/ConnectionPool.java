package com.hbase.pool.hikari;

import com.hbase.pool.PoolAccessLock;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/8.
 */
public class ConnectionPool {

    private Set<Runnable> addConnectionQueue;

    private PoolAccessLock poolAccessLock;

    private ScheduledExecutorService houseKeepingExecutorService;

    private ConnectionPoolManager connectionPoolManager;
}
