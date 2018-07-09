package com.hbase.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hbase.annotation.*;
import com.hbase.exception.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
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

import com.hbase.exception.ConfigurationException;

/**
 * Created by leon on 2017/4/11.
 */
@Configuration
public class HbaseTableScanHandler implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private ResourcePatternResolver resourcePatternResolver =
                                                            new PathMatchingResourcePatternResolver();
    
    private ClassLoader classLoader;
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {
        if (!metadata.isAnnotated(HbaseTableScan.class.getName())) {
            log.info("bootstrap class must config @HbaseTableScan");
        }
        AnnotationAttributes attributes =
                                        AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(HbaseTableScan.class.getName()));
        String packageName = attributes.getString("value");
        
        Set<Class> classes = scanPackage(packageName);
        Set<Htable> htables = classes.stream()
                                     .filter(clazz -> clazz.isAnnotationPresent(HbaseTable.class))
                                     .map(this::resolveAnnotationClass)
                                     .collect(Collectors.toSet());
        log.info(htables.toArray());
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
            throw new ParseException("read resource file error");
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
                throw new ParseException("parse resource file error");
            }
            classes.add(clazz);
        });
        return classes;
    }
    
    private Htable resolveAnnotationClass(Class clazz) {
        HbaseTable hbaseTable = (HbaseTable) clazz.getAnnotation(HbaseTable.class);
        String tableName = hbaseTable.name();
        String rowkey = null;
        
        for (Field field : Arrays.asList(clazz.getDeclaredFields())) {
            if (field.isAnnotationPresent(com.hbase.annotation.RowKey.class)) {
                rowkey = field.getName();
            }
        }
        if (StringUtils.isBlank(rowkey)) {
            throw new ConfigurationException("must config rowkey field");
        }
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
                throw new ConfigurationException("differnet column family has reduplicated field");
            }
            columnListConfiged.forEach(column -> {
                if (!columnsWithField.contains(column)) {
                    throw new ConfigurationException(column
                                                     + " config column list can not be found on class");
                }
            });
        }
        return new Htable(tableName, rowkey, columnFamilyMap, columnsWithField);
    }
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.classLoader = resourceLoader.getClassLoader();
    }
}
