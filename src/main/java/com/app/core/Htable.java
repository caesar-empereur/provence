package com.app.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by leon on 2018/4/19.
 */
public class Htable {
    
    private RowKey rowKey;
    
    private Map<String, Set<String>> columnsWithFamily = new LinkedHashMap();
    
    private Map columnWithoutFamily = new LinkedHashMap();
    
    private boolean hasColumnFamily;
    
    public Htable(RowKey rowKey,
                  Map<String, Set<String>> columnsWithFamily,
                  Map columnWithoutFamily,
                  boolean hasColumnFamily) {
        this.rowKey = rowKey;
        this.columnsWithFamily = columnsWithFamily;
        this.columnWithoutFamily = columnWithoutFamily;
        this.hasColumnFamily = hasColumnFamily;
    }
    
    public RowKey getRowKey() {
        return rowKey;
    }
    
    public void setRowKey(RowKey rowKey) {
        this.rowKey = rowKey;
    }
    
    public Map<String, Set<String>> getColumnsWithFamily() {
        return columnsWithFamily;
    }
    
    public void setColumnsWithFamily(Map<String, Set<String>> columnsWithFamily) {
        this.columnsWithFamily = columnsWithFamily;
    }
    
    public Map getColumnWithoutFamily() {
        return columnWithoutFamily;
    }
    
    public void setColumnWithoutFamily(Map columnWithoutFamily) {
        this.columnWithoutFamily = columnWithoutFamily;
    }
    
    public boolean isHasColumnFamily() {
        return hasColumnFamily;
    }
    
    public void setHasColumnFamily(boolean hasColumnFamily) {
        this.hasColumnFamily = hasColumnFamily;
    }
}
