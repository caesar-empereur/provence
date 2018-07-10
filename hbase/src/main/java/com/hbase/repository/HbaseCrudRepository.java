package com.hbase.repository;

/**
 * @author yingyang
 * @date 2018/7/9.
 */
public interface HbaseCrudRepository<RK, M> {
    
    M save(M model);
    
    Iterable<M> saveAll(Iterable<M> models);
    
    void deleteByRowkey(RK rowkey);
    
    void deleteAll();
    
    void deleteAll(Iterable<RK> rowkeys);
    
    long count();
    
    Iterable<M> findByRowKeys(Iterable<RK> rowkeys);
    
    M findByRowkey(RK rowkey);
}
