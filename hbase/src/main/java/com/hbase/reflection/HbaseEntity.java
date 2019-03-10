package com.hbase.reflection;

import java.util.List;
import java.util.Map;

import com.hbase.core.FamilyColumn;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
public interface HbaseEntity<T, RK> {

    Class<T> getJavaType();

    RK getRowkey(T entity);

    Map<String, Class> getRowkeyColumnMap();

    String getTableName();

    boolean isNew(T entity);

    List<FamilyColumn> getFamilyColumnList();
}
