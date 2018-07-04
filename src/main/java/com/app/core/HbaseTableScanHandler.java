package com.app.core;

import java.lang.reflect.Field;
import java.util.*;

import com.app.annotation.*;
import com.app.exception.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

/**
 * Created by leon on 2017/4/11.
 */
@Configuration
public class HbaseTableScanHandler implements ImportBeanDefinitionRegistrar {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    private Set<String> packageNames;
    
    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private static ResourcePatternResolver resourcePatternResolver =
                                                                   new PathMatchingResourcePatternResolver();
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        if (!importingClassMetadata.isAnnotated(HbaseTableScan.class.getName())) {
            log.info("启动类 必须加上 HbaseTableScan 注解");
        }
        AnnotationAttributes attributes =
                                        AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(HbaseTableScan.class.getName()));
        String packageName = attributes.getString("value");
        try {
            Set<Class> classes = scanPackage(packageName);
            resolveModel(classes);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Set<Class> scanPackage(String packageName) throws Exception {
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                         + ClassUtils.convertClassNameToResourcePath(packageName)
                         + CLASS_RESOURCE_PATTERN;
        Resource[] resources = resourcePatternResolver.getResources(pattern);
        MetadataReaderFactory readerFactory =
                                            new CachingMetadataReaderFactory(resourcePatternResolver);
        
        Set<Class> classes = new LinkedHashSet<>();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                
                Class clazz = classLoader.loadClass(className);
                
                classes.add(clazz);
            }
        }
        return classes;
    }
    
    public static Set<Htable> resolveModel(Set<Class> classes) {
        Set<Htable> htables = new LinkedHashSet<>();
        for (Class clazz : classes) {
            if (!clazz.isAnnotationPresent(HbaseTable.class)) {
                continue;
            }
            HbaseTable hbaseTable = (HbaseTable) clazz.getAnnotation(HbaseTable.class);
            
            String tableName = hbaseTable.name();
            String rowkey = null;
            Set<String> columnsWithField = new HashSet<>();

            Field[] fields = clazz.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.isAnnotationPresent(com.app.annotation.RowKey.class)) {
                    rowkey = field.getName();
                }
                if (field.isAnnotationPresent(HTableColum.class)) {
                    columnsWithField.add(field.getName());
                }
            }
            
            Map<String, Set<String>> columnFamilySaved = new HashMap<>();
            if (clazz.isAnnotationPresent(CompoundColumFamily.class)) {
                CompoundColumFamily compoundColumFamily =
                                                        (CompoundColumFamily) clazz.getAnnotation(CompoundColumFamily.class);
                ColumnFamily[] columnFamilies = compoundColumFamily.columnFamily();
                List<String> columnListSaved = new ArrayList<>();
                for (int i = 0; i < columnFamilies.length; i++) {
                    ColumnFamily columnFamily = columnFamilies[i];
                    String[] columnList = columnFamily.columnList();
                    columnFamilySaved.put(columnFamily.name(),
                                          new HashSet<>(Arrays.asList(columnList)));
                    columnListSaved.addAll(Arrays.asList(columnList));
                }
                if (columnListSaved.size() != new HashSet<String>(columnListSaved).size()) {
                    throw new ConfigurationException("不同 column family 出现重复的字段");
                }
                columnListSaved.forEach(column -> {
                    if (columnsWithField.contains(column)) {
                        throw new ConfigurationException(column + " 配置的字段在类中找不到对应的字段");
                    }
                });
            }
            
            Htable htable = new Htable(tableName, rowkey, columnFamilySaved, columnsWithField);
            htables.add(htable);
        }
        return htables;
    }
}
