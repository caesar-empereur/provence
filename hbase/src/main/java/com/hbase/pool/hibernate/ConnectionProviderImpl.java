package com.hbase.pool.hibernate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.hadoop.hbase.Stoppable;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.hbase.config.HbaseConfigProvider;
import com.hbase.pool.ConnectionProvider;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
@Component
public class ConnectionProviderImpl implements ConnectionProvider, Stoppable, InitializingBean {
    
    private boolean active = false;
    
    private ScheduledExecutorService executorService;
    
    private ConnectionPool connectionPool;

    @Resource
    private HbaseConfigProvider hbaseConfig;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        connectionPool = createPool();
    }
    
    @Override
    public Connection getConnection() {
        if (!active) {
            throw new IllegalStateException("Connection pool is no longer active");
        }
        return connectionPool.poll();
    }
    
    private ConnectionPool createPool() {
        return new ConnectionPool(hbaseConfig.getMaxSize(),
                                  hbaseConfig.getMinSize(),
                                  hbaseConfig.getInitSize(),
                                  hbaseConfig.getInterval(),
                                  hbaseConfig.getQuorum());
    }
    
    @Override
    public void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        connectionPool.add(conn);
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
        connectionPool.close();
    }
    
    @Override
    public boolean isStopped() {
        return !active;
    }
}
