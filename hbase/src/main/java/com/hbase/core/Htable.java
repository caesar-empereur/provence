package com.hbase.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Created by leon on 2018/4/19.
 */
@Data
public class Htable implements Serializable{
    
    private Class modelClass;
    
    private String tableName;
    
    private Map<String, Class> rowKeyColumns;
    
    public Htable(Class modelClass,
                  String tableName,
                  Map<String, Class> rowKeyColumns) {
        this.modelClass = modelClass;
        this.tableName = tableName;
        this.rowKeyColumns = rowKeyColumns;
    }

}
