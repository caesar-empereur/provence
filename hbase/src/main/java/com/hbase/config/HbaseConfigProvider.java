package com.hbase.config;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public interface HbaseConfigProvider {

    String getQuorum();
    
    Integer getInitSize();
    
    Integer getMinSize();
    
    Integer getMaxSize();
    
    Integer getInterval();
}
