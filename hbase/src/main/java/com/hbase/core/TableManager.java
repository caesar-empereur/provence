package com.hbase.core;

/**
 * Created by leon on 2018/4/19.
 */
public interface TableManager<M, RK> {

    void save(M model);

    M update(M model);

    void remove(M model);

    M find(Class<M> modelClass, RK rowkey);

    boolean contains(M model);

}
