package com.hbase.repository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/9.
 */
public interface RowkeyGenerator<T, RK> {

    RK getRowkey(T entity);
}
