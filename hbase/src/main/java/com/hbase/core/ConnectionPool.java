package com.hbase.core;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Created by yang on 2018/3/5.
 */
@Component
public class ConnectionPool implements InitializingBean {
    
    @Resource
    private HbaseConfigProvider hbaseConfig;
    
    private Configuration configuration;
    
    private Connection connection;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        configuration = HBaseConfiguration.create();
        configuration.set(hbaseConfig.getHbaseName(), hbaseConfig.getHbaseValue());
        try {
            connection = ConnectionFactory.createConnection(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        return connection;
    }
}
