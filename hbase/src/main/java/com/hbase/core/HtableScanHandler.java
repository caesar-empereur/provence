package com.hbase.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.alibaba.fastjson.JSON;
import com.hbase.annotation.*;
import com.hbase.repository.DefaultHbaseCrudRepository;
import com.hbase.repository.HbaseCrudRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hbase.exception.ConfigurationException;
import com.hbase.util.ClassParser;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Created by leon on 2017/4/11.
 */
public class HtableScanHandler {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    public static final ConcurrentHashMap<Class, Htable> TABLE_CONTAINNER =
                                                                          new ConcurrentHashMap<>();
    
    private HtableScanHandler(Builder builder) {
        Set<Class> modelClasses = ClassParser.scanPackage(builder.modelPackageName);
        Set<Class> repositoryClasses = ClassParser.scanPackage(builder.repositoryPackageName);
        
        Set<Htable> htables = modelClasses.stream()
                                          .filter(clazz -> clazz.isAnnotationPresent(HbaseTable.class)
                                                         && clazz.isAnnotationPresent(RowKey.class))
                                          .map(this::resolveModelClass)
                                          .collect(Collectors.toSet());
        Set<HbaseRepositoryInfo> repositorySet = repositoryClasses.stream()
                                                                  .filter(clazz -> clazz.isAnnotationPresent(HbaseRepository.class))
                                                                  .map(this::resolveRepositoryClass)
                                                                  .collect(Collectors.toSet());
        log.info("解析到表结构: " + JSON.toJSONString(htables));
        // TableInitDelegate.initHbaseTable(htables);
    }
    
    private Htable resolveModelClass(Class clazz) {
        Set<Field> allFieldSet = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        Set<String> allFieldStringSet = allFieldSet.stream()
                                                   .map(Field::getName)
                                                   .collect(Collectors.toSet());

        HbaseTable hbaseTable = (HbaseTable) clazz.getAnnotation(HbaseTable.class);
        String tableName = hbaseTable.name();
        RowKey rowKey = (RowKey) clazz.getAnnotation(RowKey.class);
        Set<String> configuredRowkeyColumnSet = new HashSet<>(Arrays.asList(rowKey.columnList()));

        Map<String, Class> rowkeyColumnMap = new HashMap<>();
        /* 校验配置的 rowkey 是否是真实存在的字段  */
        for (String rowkeyColumn : configuredRowkeyColumnSet){
            if (!allFieldStringSet.contains(rowkeyColumn)){
                throw new ConfigurationException("找不到配置的 rowkey: " + rowkeyColumn);
            }
            for (Field field : allFieldSet) {
                if (field.getName().equals(rowkeyColumn)) {
                    Class fieldClass = field.getType();
                    if (fieldClass == String.class || fieldClass == Date.class
                        || fieldClass == Number.class) {
                        rowkeyColumnMap.put(rowkeyColumn, field.getClass());
                    }
                    else {
                        throw new ConfigurationException("rowkey 字段不支持该类型: " + fieldClass);
                    }
                }
            }
        }
        Htable htable = new Htable(clazz, tableName, rowkeyColumnMap);
        TABLE_CONTAINNER.put(clazz, htable);
        return htable;
    }
    
    private HbaseRepositoryInfo resolveRepositoryClass(Class clazz) {
        HbaseRepositoryInfo hbaseRepositoryInfo = new HbaseRepositoryInfo();
        for (Type repositoryType : clazz.getGenericInterfaces()) {
            if (repositoryType instanceof ParameterizedType
                && HbaseCrudRepository.class == ((ParameterizedTypeImpl) repositoryType).getRawType()) {
                for (Type modelType : ((ParameterizedTypeImpl) repositoryType).getActualTypeArguments()) {
                    Class modelClass = (Class) modelType;
                    if (TABLE_CONTAINNER.containsKey(modelClass)) {
                        hbaseRepositoryInfo.setModelClass(modelClass);
                    }
                    else {
                        hbaseRepositoryInfo.setRowkey(modelClass);
                    }
                }
            }
        }
        try {
            hbaseRepositoryInfo.setHbaseCrudRepository(DefaultHbaseCrudRepository.class.newInstance());
        }
        catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return hbaseRepositoryInfo;
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
