package com.hbase.core;

import com.hbase.repository.HbaseCrudRepository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
public class HbaseRepositoryInfo {

    private HbaseCrudRepository hbaseCrudRepository;

    private Class rowkey;

    private Class modelClass;

    public HbaseCrudRepository getHbaseCrudRepository() {
        return hbaseCrudRepository;
    }

    public void setHbaseCrudRepository(HbaseCrudRepository hbaseCrudRepository) {
        this.hbaseCrudRepository = hbaseCrudRepository;
    }

    public Class getRowkey() {
        return rowkey;
    }

    public void setRowkey(Class rowkey) {
        this.rowkey = rowkey;
    }

    public Class getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class modelClass) {
        this.modelClass = modelClass;
    }
}
