package com.hbase.core;

import org.apache.hadoop.hbase.client.Connection;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public interface Session<M, RK> extends TableManager<M, RK> {

    Connection disconnect();

    Connection connection();

}
