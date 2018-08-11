package com.hbase.pool.hikari;

import com.hbase.config.HbaseConfigProvider;
import com.hbase.pool.ConnectionProvider;
import org.apache.hadoop.hbase.client.Connection;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/11.
 */
public class DefaultConnectionProvider implements ConnectionProvider {

    private ConnectionPool connectionPool;

    private HbaseConfigProvider configProvider;

    public DefaultConnectionProvider(HbaseConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public Connection getConnection() {
        if(connectionPool ==null){
            synchronized (this){
                if (connectionPool==null){
                    connectionPool = new ConnectionPool(configProvider);
                }
            }
        }
//        return connectionPool
        return null;
    }
    
    @Override
    public void closeConnection(Connection conn) {
        
    }
}
