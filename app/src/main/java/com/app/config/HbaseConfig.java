package com.app.config;

import com.hbase.config.HbaseConfigProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
@Component
public class HbaseConfig implements HbaseConfigProvider {
    
    @Value("${hbase-name}")
    private String hbaseName;
    
    @Value("${hbase-value}")
    private String hbaseValue;
    
    @Override
    public String getHbaseName() {
        return hbaseName;
    }
    
    @Override
    public String getHbaseValue() {
        return hbaseValue;
    }
    
    @Override
    public int getMaxPoolSize() {
        return 0;
    }
    
    @Override
    public int getMinPoolSize() {
        return 0;
    }
    
    @Override
    public int getInitPoolSize() {
        return 0;
    }
    
    @Override
    public int getCheckInterval() {
        return 0;
    }
}
