package com.hbase.pool.hibernate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hbase.config.ConnectionSizeConfig;
import com.hbase.pool.hibernate.ConnectionProvider;
import com.hbase.pool.hibernate.PooledConnections;
import org.apache.hadoop.hbase.Stoppable;
import org.apache.hadoop.hbase.client.Connection;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class ConnectionProviderImpl implements ConnectionProvider, Stoppable, InitializingBean {
    
    private boolean active = false;
    
    private ScheduledExecutorService executorService;
    
    private PooledConnections pooledConnections;
    
    @Resource
    private ConnectionSizeConfig connectionSizeConfig;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        pooledConnections = buildPool();
        final long validationInterval = connectionSizeConfig.getInterval().longValue();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> pooledConnections.validate(),
                                               validationInterval,
                                               validationInterval,
                                               TimeUnit.SECONDS);
    }
    
    @Override
    public Connection getConnection() {
        if (!active) {
            throw new IllegalStateException("Connection pooledConnections is no longer active");
        }
        return pooledConnections.poll();
    }
    
    private PooledConnections buildPool() {
        PooledConnections.Builder builder = new PooledConnections.Builder();
        builder.initialSize(connectionSizeConfig.getInitSize());
        builder.minSize(connectionSizeConfig.getMinSize());
        builder.maxSize(connectionSizeConfig.getMaxSize());
        return builder.build();
    }
    
    @Override
    public void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        pooledConnections.add(conn);
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
        pooledConnections.close();
    }
    
    @Override
    public boolean isStopped() {
        return !active;
    }
}
