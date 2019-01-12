package com.hbase.core;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
public class HbaseRepositoryInfo {

    private Class repositoryClass;

    private Class rowkey;

    private Class modelClass;

    public Class getRepositoryClass() {
        return repositoryClass;
    }

    public void setRepositoryClass(Class repositoryClass) {
        this.repositoryClass = repositoryClass;
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
