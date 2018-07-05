package com.hbase.core;

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
    
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        
        Htable htable = (Htable) o;
        
        if (hasColumnFamily != htable.hasColumnFamily)
            return false;
        if (tableName != null ? !tableName.equals(htable.tableName) : htable.tableName != null)
            return false;
        if (rowKey != null ? !rowKey.equals(htable.rowKey) : htable.rowKey != null)
            return false;
        if (columnsWithFamily != null ? !columnsWithFamily.equals(htable.columnsWithFamily)
                                      : htable.columnsWithFamily != null)
            return false;
        return columnWithoutFamily != null ? columnWithoutFamily.equals(htable.columnWithoutFamily)
                                           : htable.columnWithoutFamily == null;
    }
    
    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + (rowKey != null ? rowKey.hashCode() : 0);
        result = 31 * result + (columnsWithFamily != null ? columnsWithFamily.hashCode() : 0);
        result = 31 * result + (columnWithoutFamily != null ? columnWithoutFamily.hashCode() : 0);
        result = 31 * result + (hasColumnFamily ? 1 : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "Htable{" + "tableName='"
               + tableName
               + '\''
               + ", rowKey='"
               + rowKey
               + '\''
               + ", columnsWithFamily="
               + columnsWithFamily
               + ", columnWithoutFamily="
               + columnWithoutFamily
               + ", hasColumnFamily="
               + hasColumnFamily
               + '}';
    }
}
