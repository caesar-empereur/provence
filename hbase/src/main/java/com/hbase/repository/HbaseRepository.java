package com.hbase.repository;

import org.springframework.data.repository.CrudRepository;

/**
 * @author yingyang
 * @date 2018/7/9.
 */
public interface HbaseRepository<T, ID> extends CrudRepository<T, ID> {
    
}
