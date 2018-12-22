package com.hbase.core;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hbase.annotation.CompoundColumFamily;
import com.hbase.annotation.HTableColum;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;
import com.hbase.exception.ConfigurationException;
import com.hbase.util.ClassParser;

/**
 * Created by leon on 2017/4/11.
 */
public class HtableScanHandler {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    public static final ConcurrentHashMap<Class, Htable> TABLE_CONTAINNER =
                                                                          new ConcurrentHashMap<>();
    
    private HtableScanHandler(Builder builder) {
        String packName = builder.modelPackageName;
        Set<Class> classes = ClassParser.scanPackage(packName);
        Set<Htable> htables = classes.stream()
                                     .filter(clazz -> clazz.isAnnotationPresent(HbaseTable.class)
                                                      && clazz.isAnnotationPresent(RowKey.class))
                                     .map(this::resolveModelClass)
                                     .collect(Collectors.toSet());
        log.info("解析到表结构: " + htables.toArray());
        TableInitDelegate.initHbaseTable(htables);
    }
    
    private Htable resolveModelClass(Class clazz) {
        HbaseTable hbaseTable = (HbaseTable) clazz.getAnnotation(HbaseTable.class);
        String tableName = hbaseTable.name();
        RowKey rowKey = (RowKey) clazz.getAnnotation(RowKey.class);
        Set<String> rowkeyColumns = new HashSet<>(Arrays.asList(rowKey.columnList()));
        Set<String> columnsWithField =
                                     Stream.of(clazz.getDeclaredFields())
                                           .filter(field -> field.isAnnotationPresent(HTableColum.class))
                                           .map(Field::getName)
                                           .collect(Collectors.toSet());
        Map<String, Set<String>> columnFamilyMap = new HashMap<>();
        if (clazz.isAnnotationPresent(CompoundColumFamily.class)) {
            CompoundColumFamily compoundColumFamily =
                                                    (CompoundColumFamily) clazz.getAnnotation(CompoundColumFamily.class);
            List<String> columnListConfiged = new ArrayList<>();
            Stream.of(compoundColumFamily.columnFamily()).forEach(columnFamily -> {
                columnFamilyMap.put(columnFamily.name(),
                                    new HashSet<>(Arrays.asList(columnFamily.columnList())));
                columnListConfiged.addAll(Arrays.asList(columnFamily.columnList()));
            });
            if (columnListConfiged.size() != new HashSet<>(columnListConfiged).size()) {
                throw new ConfigurationException("不同 ColumnFamily 不能包含相同 column");
            }
            columnListConfiged.forEach(column -> {
                if (!columnsWithField.contains(column)) {
                    throw new ConfigurationException(column + "找不到配置的列");
                }
            });
        }
        Htable htable = new Htable(tableName, rowkeyColumns, columnFamilyMap, columnsWithField);
        TABLE_CONTAINNER.put(clazz, htable);
        return htable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        
        private String modelPackageName;
        
        private String repositoryPackageName;
        
        public Builder withModelPackageName(String modelPackageName) {
            this.modelPackageName = modelPackageName;
            return this;
        }
        
        public Builder withRepositoryPackageName(String repositoryPackageName) {
            this.repositoryPackageName = repositoryPackageName;
            return this;
        }
        
        public HtableScanHandler build() {
            return new HtableScanHandler(this);
        }
    }
    
}
