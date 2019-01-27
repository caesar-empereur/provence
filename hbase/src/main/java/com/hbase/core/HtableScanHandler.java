package com.hbase.core;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hbase.reflection.ReflectManeger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
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
import org.springframework.data.repository.config.RepositoryBeanNameGenerator;
import org.springframework.util.ClassUtils;

import com.hbase.annotation.HbaseRepository;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.HbaseTableScan;
import com.hbase.annotation.RowKey;
import com.hbase.exception.ConfigurationException;
import com.hbase.exception.ParseException;
import com.hbase.repository.HbaseCrudRepository;
import com.hbase.repository.HbaseRepositoryFactoryBean;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Created by leon on 2017/4/11.
 */
public class HtableScanHandler implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private final Function<String, Set<Class>> CLASS_RESOLVER = this::scanPackage;
    
    private Supplier<ResourcePatternResolver> resourcePatternResolver =
                                                                      PathMatchingResourcePatternResolver::new;
    
    private Supplier<RepositoryBeanNameGenerator> beanNameGenerator =
                                                                    () -> new RepositoryBeanNameGenerator(this.getClass()
                                                                                                              .getClassLoader());
    
    private static final BiFunction<AnnotatedElement, Class<? extends Annotation>, Boolean> IS_ANNOTATED =
                                                                                            AnnotatedElement::isAnnotationPresent;
    
    private ClassLoader classLoader;
    
    public static final ConcurrentMap<Class, Htable> TABLE_CONTAINNER = new ConcurrentHashMap<>();
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes =
                                        AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(HbaseTableScan.class.getName()));
        String modelPackageName = attributes.getString("modelPackage");
        String repositoryPackageName = attributes.getString("repositoryPackage");
        
        Set<Class> modelClasses = CLASS_RESOLVER.apply(modelPackageName);
        Set<Class> repositoryClasses = CLASS_RESOLVER.apply(repositoryPackageName);
        
        Set<Htable> htables = modelClasses.stream()
                                          .map(this::resolveModelClass)
                                          .filter(this::equals)
                                          .collect(Collectors.toSet());
        
        Set<HbaseRepositoryInfo> repositorySet = repositoryClasses.stream()
                                                                  .map(this::resolveRepositoryClass)
                                                                  .filter(this::equals)
                                                                  .collect(Collectors.toSet());
        htables.forEach(table -> TABLE_CONTAINNER.put(table.getModelClass().get(), table));
        registerRepository(repositorySet, registry);
    }
    
    private void registerRepository(Set<HbaseRepositoryInfo> hbaseRepositoryInfos,
                                    BeanDefinitionRegistry registry) {
        for (HbaseRepositoryInfo info : hbaseRepositoryInfos) {
            BeanDefinitionBuilder beanDefinitionBuilder =
                                                        BeanDefinitionBuilder.rootBeanDefinition(HbaseRepositoryFactoryBean.class);
            beanDefinitionBuilder.addConstructorArgValue(info.getRepositoryClass());
            AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            String beanName = beanNameGenerator.get().generateBeanName(beanDefinition);
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }
    
    private Set<Class> scanPackage(String packageName) {
        Optional.ofNullable(packageName)
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new ParseException(""));
        Set<Class> classes = new LinkedHashSet<>();
        
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                         + ClassUtils.convertClassNameToResourcePath(packageName)
                         + CLASS_RESOURCE_PATTERN;
        Resource[] resources;
        try {
            resources = this.resourcePatternResolver.get().getResources(pattern);
        }
        catch (IOException e) {
            throw new ParseException("解析类异常");
        }
        MetadataReaderFactory readerFactory =
                                            new CachingMetadataReaderFactory(this.resourcePatternResolver.get());
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
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (!IS_ANNOTATED.apply(clazz, HbaseTable.class) || !IS_ANNOTATED.apply(clazz, RowKey.class)) {
            return null;
        }
        if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        Set<Field> allFieldSet = ReflectManeger.getAllField(clazz);

        Set<String> allFieldStringSet = allFieldSet.stream()
                                                   .map(Field::getName)
                                                   .collect(Collectors.toSet());
        
        HbaseTable hbaseTable = (HbaseTable) clazz.getAnnotation(HbaseTable.class);
        String tableName = hbaseTable.name();
        RowKey rowKey = (RowKey) clazz.getAnnotation(RowKey.class);
        Set<String> configuredRowkeyColumnSet = new HashSet<>(Arrays.asList(rowKey.columnList()));
        
        Class rowkeyClass = null;
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
                        rowkeyClass = fieldClass;
                    }
                    else {
                        throw new ConfigurationException("rowkey 字段不支持该类型: " + fieldClass);
                    }
                }
            }
        }
        return new Htable(clazz, tableName, rowkeyColumnMap);
    }
    

    
    private HbaseRepositoryInfo resolveRepositoryClass(Class clazz) {
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (IS_ANNOTATED.apply(clazz, HbaseRepository.class) ) {
            return null;
        }
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
            else {
                return null;
            }
        }
        hbaseRepositoryInfo.setRepositoryClass(clazz);
        return hbaseRepositoryInfo;
    }
    
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.classLoader = resourceLoader.getClassLoader();
    }
    
}
