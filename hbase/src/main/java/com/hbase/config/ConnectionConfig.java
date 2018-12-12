package com.hbase.config;

import lombok.Data;

/**
 * @author yingyang
 * @date 2018/7/12.
 */
@Data
public class ConnectionConfig {

    private Integer initSize;

    private Integer minSize;

    private Integer maxSize;

    private Integer validateInterval;

}
