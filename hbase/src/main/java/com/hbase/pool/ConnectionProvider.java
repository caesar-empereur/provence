package com.hbase.pool;

import org.apache.hadoop.hbase.client.Connection;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public interface ConnectionProvider<C extends Connection> {

    C getConnection();

    void recycleConnection(C connection);
}
