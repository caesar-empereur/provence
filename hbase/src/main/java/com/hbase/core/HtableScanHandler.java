package com.hbase.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import com.alibaba.fastjson.JSON;
import com.hbase.annotation.HbaseRepository;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.HbaseTableScan;
import com.hbase.annotation.RowKey;
import com.hbase.exception.ConfigurationException;
import com.hbase.exception.ParseException;
import com.hbase.repository.DefaultHbaseCrudRepository;
import com.hbase.repository.HbaseCrudRepository;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Created by leon on 2017/4/11.
 */
public class HtableScanHandler implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private ResourcePatternResolver resourcePatternResolver =
                                                            new PathMatchingResourcePatternResolver();
    
    private ClassLoader classLoader;
    
    public static final ConcurrentHashMap<Class, Htable> TABLE_CONTAINNER =
                                                                          new ConcurrentHashMap<>();
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes =
                                        AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(HbaseTableScan.class.getName()));
        String modelPackageName = attributes.getString("modelPackage");
        String repositoryPackageName = attributes.getString("repositoryPackage");
        
        Set<Class> modelClasses = scanPackage(modelPackageName);
        Set<Class> repositoryClasses = scanPackage(repositoryPackageName);
        
        Set<Htable> htables =
                            modelClasses.stream()
                                        .filter(clazz -> clazz.isAnnotationPresent(HbaseTable.class)
                                                         && clazz.isAnnotationPresent(RowKey.class))
                                        .map(this::resolveModelClass)
                                        .collect(Collectors.toSet());
        Set<HbaseRepositoryInfo> repositorySet =
                                               repositoryClasses.stream()
                                                                .filter(clazz -> clazz.isAnnotationPresent(HbaseRepository.class))
                                                                .map(this::resolveRepositoryClass)
                                                                .collect(Collectors.toSet());
        log.info("解析到表结构: " + JSON.toJSONString(htables));
    }
    
    private Set<Class> scanPackage(String packageName) {
        Set<Class> classes = new LinkedHashSet<>();
        
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                         + ClassUtils.convertClassNameToResourcePath(packageName)
                         + CLASS_RESOURCE_PATTERN;
        Resource[] resources;
        try {
            resources = this.resourcePatternResolver.getResources(pattern);
        }
        catch (IOException e) {
            throw new ParseException("解析类异常");
        }
        MetadataReaderFactory readerFactory =
                                            new CachingMetadataReaderFactory(this.resourcePatternResolver);
        Stream.of(resources).filter(Resource::isReadable).forEach(resource -> {
            Class clazz;
            try {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                clazz = ClassUtils.forName(className, this.classLoader);
            }
            catch (IOException | ClassNotFoundException e) {
                throw new ParseException("解析配置类异常");
            }
            classes.add(clazz);
        });
        return classes;
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
        /* 校验配置的 rowkey 是否是真实存在的字段 */
        for (String rowkeyColumn : configuredRowkeyColumnSet) {
            if (!allFieldStringSet.contains(rowkeyColumn)) {
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
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.classLoader = resourceLoader.getClassLoader();
    }
    
}
