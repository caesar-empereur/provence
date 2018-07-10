package com.hbase.core;

/**
 * Created by leon on 2018/4/19.
 */
public interface HtableManager<M, RK> {

    void persist(M model);

    M merge(M model);

    void remove(M model);

    M find(Class<M> modelClass, RK rowkey);

    boolean contains(M model);


}
