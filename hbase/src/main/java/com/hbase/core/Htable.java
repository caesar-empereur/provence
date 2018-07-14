package com.hbase.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by leon on 2018/4/19.
 */
public class Htable implements Serializable{
    
    private String tableName;
    
    private Set<String> rowKeyColumns;
    
    private Map<String, Set<String>> columnsWithFamily;
    
    private Set<String> columnWithoutFamily;
    
    private boolean hasColumnFamily;
    
    public Htable(String tableName,
                  Set<String> rowKeyColumns,
                  Map<String, Set<String>> columnsWithFamily,
                  Set<String> columnWithoutFamily) {
        this.tableName = tableName;
        this.rowKeyColumns = rowKeyColumns;
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

    public Set<String> getRowKeyColumns() {
        return rowKeyColumns;
    }

    public void setRowKeyColumns(Set<String> rowKeyColumns) {
        this.rowKeyColumns = rowKeyColumns;
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
        if (rowKeyColumns != null ? !rowKeyColumns.equals(htable.rowKeyColumns) : htable.rowKeyColumns != null)
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
        result = 31 * result + (rowKeyColumns != null ? rowKeyColumns.hashCode() : 0);
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
               + rowKeyColumns
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
