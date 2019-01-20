package com.hbase.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * Created by leon on 2018/4/19.
 */
@Data
public class Htable implements Serializable{
    
    private Optional<Class> modelClass = Optional.empty();
    
    private Optional<String> tableName = Optional.empty();
    
    private Optional<Map<String, Class>> rowKeyColumns = Optional.empty();
    
    public Htable(Class modelClass,
                  String tableName,
                  Map<String, Class> rowKeyColumns) {
        this.modelClass = Optional.ofNullable(modelClass);
        this.tableName = Optional.ofNullable(tableName);
        this.rowKeyColumns = Optional.ofNullable(rowKeyColumns);
    }

}
