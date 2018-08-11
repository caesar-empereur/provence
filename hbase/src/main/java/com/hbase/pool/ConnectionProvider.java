package com.hbase.pool;

import org.apache.hadoop.hbase.client.Connection;

import java.sql.SQLException;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public interface ConnectionProvider {

    Connection getConnection();

    void closeConnection(Connection conn);
}
