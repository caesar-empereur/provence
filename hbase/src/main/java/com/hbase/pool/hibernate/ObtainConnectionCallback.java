package com.hbase.pool.hibernate;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/13.
 */
public interface ObtainConnectionCallback {

    void onCreateConnection();
}
