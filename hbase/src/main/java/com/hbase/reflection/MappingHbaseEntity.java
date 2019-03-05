package com.hbase.reflection;

import com.hbase.core.FamilyColumn;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
@SuppressWarnings("all")
public class MappingHbaseEntity<T, ID> implements HbaseEntity<T, ID> {

    private Class<T> modelClass;

    private Class<ID> idClass;

    private String tableName;

    private Map<String, Class> rowKeyColumns;

    private List<FamilyColumn> familyColumnList;

    public MappingHbaseEntity(Class<T> modelClass,
                              String tableName,
                              Class<ID> idClass,
                              Map<String, Class> rowKeyColumns,
                              List<FamilyColumn> familyColumnList) {
        this.modelClass = Optional.of(modelClass).get();
        this.tableName = Optional.of(tableName).get();
        this.idClass = Optional.of(idClass).get();
        this.rowKeyColumns = Optional.of(rowKeyColumns).get();
        this.familyColumnList = Optional.of(familyColumnList).get();
    }

    public List<FamilyColumn> getFamilyColumnList() {
        return familyColumnList;
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
