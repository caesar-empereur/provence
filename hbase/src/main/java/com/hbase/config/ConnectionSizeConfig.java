package com.hbase.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yingyang
 * @date 2018/7/12.
 */
@ConfigurationProperties(prefix = "pool.connection")
@Component
public class ConnectionSizeConfig {

    @Value("${init-size}")
    private Integer initSize;

    @Value("${min-size}")
    private Integer minSize;

    @Value("${max-size}")
    private Integer maxSize;

    @Value("${validate-interval}")
    private Integer interval;

    public Integer getInitSize() {
        return initSize;
    }

    public Integer getMinSize() {
        return minSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public Integer getInterval() {
        return interval;
    }
}
