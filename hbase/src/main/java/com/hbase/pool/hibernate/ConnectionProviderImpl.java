package com.hbase.pool.hibernate;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

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
public class ConnectionProviderImpl implements ConnectionProvider, InitializingBean {
    
    private boolean active = false;
    
    private ScheduledExecutorService executorService;
    
    private ConnectionPool connectionPool;
    
    @Resource
    private HbaseConfigProvider hbaseConfig;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        connectionPool = new ConnectionPool(hbaseConfig.getMaxSize(),
                                            hbaseConfig.getMinSize(),
                                            hbaseConfig.getInitSize(),
                                            hbaseConfig.getQuorum());

        final long validationInterval = hbaseConfig.getInterval().longValue();
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> connectionPool.validate(),
                                               0,
                                               validationInterval,
                                               TimeUnit.SECONDS);
    }
    
    @Override
    public Connection getConnection() {
        if (!active) {
            throw new IllegalStateException("Connection pool is no longer active");
        }
        return connectionPool.poll();
    }
    
    @Override
    public void closeConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
