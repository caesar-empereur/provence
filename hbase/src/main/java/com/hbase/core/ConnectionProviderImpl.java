package com.hbase.core;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Resource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Stoppable;
import org.apache.hadoop.hbase.client.Connection;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionProviderImpl implements ConnectionProvider, Stoppable {
    
    private boolean active = true;
    
    private ScheduledExecutorService executorService;
    
    private PooledConnections pool;

    @Resource
    private HbaseConfigProvider hbaseConfig;

    private Configuration configuration;

    @Override
    public Connection getConnection() {
        if (!active) {
            throw new IllegalStateException("Connection pool is no longer active");
        }
        return pool.poll();
    }



    @Override
    public void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        pool.add(conn);
    }
    
    @Override
    public void stop(String why) {
        if (!active) {
            return;
        }
        active = false;
        if (executorService != null) {
            executorService.shutdown();
        }
        executorService = null;
        pool.close();
    }
    
    @Override
    public boolean isStopped() {
        return !active;
    }
}
