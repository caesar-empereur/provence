package com.hbase.reflection;

import com.hbase.core.FamilyColumn;
import com.hbase.repository.RowkeyGenerator;
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
public class MappingHbaseEntity<T, RK> implements HbaseEntity<T, RK> {

    private Class<T> modelClass;

    private String tableName;

    private List<FamilyColumn> familyColumnList;

    private Map<String, Class> rowkeyColumnMap;

    private RowkeyGenerator<T, RK> rowkeyGenerator;

    public MappingHbaseEntity(Class<T> modelClass,
                              String tableName,
                              Map<String, Class> rowkeyColumnMap,
                              List<FamilyColumn> familyColumnList) {
        this.modelClass = Optional.of(modelClass).get();
        this.tableName = Optional.of(tableName).get();
        this.rowkeyColumnMap = Optional.of(rowkeyColumnMap).get();
        this.familyColumnList = Optional.of(familyColumnList).get();
    }

    @Override
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
    public RK getRowkey(T entity) {
        return this.rowkeyGenerator.getRowkey(entity);
    }

    @Override
    public Map<String, Class> getRowkeyColumnMap() {
        return this.rowkeyColumnMap;
    }

    @Override
    public Class<T> getJavaType() {
        return this.modelClass;
    }

    public void setRowkeyGenerator(RowkeyGenerator<T, RK> rowkeyGenerator) {
        this.rowkeyGenerator = rowkeyGenerator;
    }
}
