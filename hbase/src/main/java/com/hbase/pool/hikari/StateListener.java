package com.hbase.pool.hikari;

/**
 * Created by yang on 2018/7/14.
 */
public interface StateListener {

    void addItem(int waiting);
}
