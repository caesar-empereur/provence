package com.hbase.reflection;

import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
@SuppressWarnings("all")
public class MappingHbaseEntityInformation<T, ID> implements HbaseEntityInformation<T, ID> {

    private Class<T> modelClass;

    private Class<ID> idClass;

    private String tableName;

    private Map<String, Class> rowKeyColumns;

    public MappingHbaseEntityInformation(Class<T> modelClass,
                                         String tableName,
                                         Class<ID> idClass,
                                         Map<String, Class> rowKeyColumns) {
        this.modelClass = Optional.of(modelClass).get();
        this.tableName = Optional.of(tableName).get();
        this.idClass = Optional.of(idClass).get();
        this.rowKeyColumns = Optional.of(rowKeyColumns).get();
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }
    
    @Override
    public boolean isNew(T entity) {
        return false;
    }
    
    @Nullable
    @Override
    public ID getId(T entity) {
        return null;
    }
    
    @Override
    public Class<ID> getIdType() {
        return this.idClass;
    }
    
    @Override
    public Class<T> getJavaType() {
        return this.modelClass;
    }
    
    @Override
    public Map<String, Class> getRowkeyColumns() {
        return this.rowKeyColumns;
    }
}
