package com.hbase.repository;

import java.util.Collection;

/**
 * @author yingyang
 * @date 2018/7/9.
 */
public interface HbaseRepository<T, RK> {

    T save(T entity);

    Collection<T> saveAll(Collection<T> entities);

    T findByRowkey(RK rk);

    Collection<T> findAllByRowkey(Collection<RK> rks);

    boolean existsByRowkey(RK id);

    long count();

    void deleteByRowkey(RK rk);

    void deleteAll(Collection<RK> entities);
}
