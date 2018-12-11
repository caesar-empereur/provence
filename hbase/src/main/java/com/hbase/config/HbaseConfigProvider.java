package com.hbase.config;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public interface HbaseConfigProvider {
    
    String getQuorum();
    
    int getMaxPoolSize();
    
    int getMinPoolSize();
    
    int getInitPoolSize();
    
    int getCheckInterval();
}
