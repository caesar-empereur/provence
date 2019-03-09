package com.hbase.reflection;

import com.hbase.core.FamilyColumn;
import com.hbase.repository.RowkeyObtain;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
@SuppressWarnings("all")
public class MappingHbaseEntity<T, RK> implements HbaseEntity<T, RK> {

    private Class<T> modelClass;

    private Class<RK> rkClass;

    private String tableName;

    private List<FamilyColumn> familyColumnList;

    private RowkeyObtain<T> rowkeyObtain;

    public MappingHbaseEntity(Class<T> modelClass,
                              String tableName,
                              Class<RK> rkClass,
                              RowkeyObtain<T> rowkeyObtain,
                              List<FamilyColumn> familyColumnList) {
        this.modelClass = Optional.of(modelClass).get();
        this.tableName = Optional.of(tableName).get();
        this.rkClass = Optional.of(rkClass).get();
        this.rowkeyObtain = Optional.of(rowkeyObtain).get();
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
    public Long getRowkey(T entity) {
        return this.rowkeyObtain.getRowkey(entity);
    }

    @Override
    public Class<RK> getRowkeyType() {
        return this.rkClass;
    }
    
    @Override
    public Class<T> getJavaType() {
        return this.modelClass;
    }
    
}
