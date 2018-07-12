package com.hbase.core;

import java.io.Serializable;

/**
 * @author yingyang
 * @date 2018/7/11.
 */
public class DefaultTableManager implements TableManager<Serializable, Bytes> {
    
    @Override
    public void save(Serializable model) {
        
    }
    
    @Override
    public Serializable update(Serializable model) {
        return null;
    }
    
    @Override
    public void remove(Serializable model) {
        
    }
    
    @Override
    public Serializable find(Class<Serializable> modelClass, Bytes rowkey) {
        return null;
    }
    
    @Override
    public boolean contains(Serializable model) {
        return false;
    }
}
