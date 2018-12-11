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
    
    @Value("${hbase.zookeeper.quorum}")
    private String quorum;

    @Value("${init-size}")
    private Integer initSize;

    @Value("${min-size}")
    private Integer minSize;

    @Value("${max-size}")
    private Integer maxSize;

    @Value("${validate-interval}")
    private Integer interval;

    @Override
    public String getQuorum() {
        return quorum;
    }

    @Override
    public Integer getInitSize() {
        return initSize;
    }

    @Override
    public Integer getMinSize() {
        return minSize;
    }

    @Override
    public Integer getMaxSize() {
        return maxSize;
    }

    @Override
    public Integer getInterval() {
        return interval;
    }
}
