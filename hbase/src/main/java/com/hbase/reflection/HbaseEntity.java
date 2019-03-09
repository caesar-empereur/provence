package com.hbase.reflection;

import java.util.List;

import com.hbase.core.FamilyColumn;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
public interface HbaseEntity<T, RK> {

    Class<T> getJavaType();

    Long getRowkey(T entity);

    Class<RK> getRowkeyType();

    String getTableName();

    boolean isNew(T entity);

    List<FamilyColumn> getFamilyColumnList();
}
