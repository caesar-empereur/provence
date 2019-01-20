package com.hbase.core;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
public class HbaseRepositoryInfo<M, R, RK> {

    private Class<R> repositoryClass;

    private Class<RK> rowkey;

    private Class<M> modelClass;

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
