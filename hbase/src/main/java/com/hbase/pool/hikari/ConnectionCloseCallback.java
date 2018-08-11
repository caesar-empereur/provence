package com.hbase.pool.hikari;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/9.
 */
@FunctionalInterface
public interface ConnectionCloseCallback {

    void onClose();
}
