package com.hbase.repository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/9.
 */
public interface RowkeyObtain<T> {

    Long getRowkey(T entity);
}
