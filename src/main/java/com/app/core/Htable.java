package com.app.core;

import java.util.Map;
import java.util.Set;

/**
 * Created by leon on 2018/4/19.
 */
public class Htable {
    
    private String tableName;
    
    private String rowKey;
    
    private Map<String, Set<String>> columnsWithFamily;
    
    private Set<String> columnWithoutFamily;
    
    private boolean hasColumnFamily;
    
    public Htable(String tableName,
                  String rowKey,
                  Map<String, Set<String>> columnsWithFamily,
                  Set<String> columnWithoutFamily) {
        this.tableName = tableName;
        this.rowKey = rowKey;
        this.columnsWithFamily = columnsWithFamily;
        this.columnWithoutFamily = columnWithoutFamily;
        this.hasColumnFamily = columnsWithFamily == null || columnsWithFamily.isEmpty();
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getRowKey() {
        return rowKey;
    }
    
    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }
    
    public Map<String, Set<String>> getColumnsWithFamily() {
        return columnsWithFamily;
    }
    
    public void setColumnsWithFamily(Map<String, Set<String>> columnsWithFamily) {
        this.columnsWithFamily = columnsWithFamily;
    }
    
    public boolean isHasColumnFamily() {
        return hasColumnFamily;
    }
    
    public void setHasColumnFamily(boolean hasColumnFamily) {
        this.hasColumnFamily = hasColumnFamily;
    }
    
    public Set<String> getColumnWithoutFamily() {
        return columnWithoutFamily;
    }
    
    public void setColumnWithoutFamily(Set<String> columnWithoutFamily) {
        this.columnWithoutFamily = columnWithoutFamily;
    }
}
