package com.hbase.config;

import lombok.Data;

/**
 * @author yingyang
 * @date 2018/7/12.
 */
@Data
public class ConnectionConfig {

    private Integer initSize = 30;

    private Integer minSize = 30;

    private Integer maxSize = 50;

    private Integer validateInterval = 30;

    private String quorum;

    private String hadoopDir;
}
