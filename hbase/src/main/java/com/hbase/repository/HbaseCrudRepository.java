package com.hbase.repository;

import java.util.Collection;

/**
 * @author yingyang
 * @date 2018/7/9.
 */
public interface HbaseCrudRepository<RK, M> {
    
    M save(M model);

    Collection<M> saveAll(Collection<M> models);
    
    void delete(M model);
    
    void deleteAll();
    
    void deleteAll(Collection<M> models);
    
    long count();

    Collection<M> findByRowKeys(Collection<RK> rowkeys);
    
    M findByRowkey(RK rowkey);
}
