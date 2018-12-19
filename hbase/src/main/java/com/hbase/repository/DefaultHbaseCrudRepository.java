package com.hbase.repository;

import java.util.Collection;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class DefaultHbaseCrudRepository implements HbaseCrudRepository {
    
    @Override
    public Object save(Object model) {
        return null;
    }
    
    @Override
    public Collection saveAll(Collection models) {
        return null;
    }
    
    @Override
    public void deleteByRowkey(Object rowkey) {
        
    }
    
    @Override
    public void deleteAll() {
        
    }
    
    @Override
    public void deleteAll(Collection rowkeys) {
        
    }
    
    @Override
    public long count() {
        return 0;
    }
    
    @Override
    public Collection findByRowKeys(Collection rowkeys) {
        return null;
    }
    
    @Override
    public Object findByRowkey(Object rowkey) {
        return null;
    }
}
