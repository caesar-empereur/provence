package com.hbase.spring;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hbase.reflection.ModelElement;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.util.ClassUtils;

import com.alibaba.fastjson.JSON;
import com.hbase.annotation.*;
import com.hbase.core.FamilyColumn;
import com.hbase.edm.EventMessage;
import com.hbase.edm.ModePrepareListener;
import com.hbase.edm.ModelPrepareEvent;
import com.hbase.exception.ConfigurationException;
import com.hbase.exception.ParseException;
import com.hbase.reflection.HbaseEntity;
import com.hbase.reflection.MappingHbaseEntity;
import com.hbase.reflection.ReflectManeger;
import com.hbase.repository.HbaseRepository;
import com.hbase.repository.HbaseRepositoryFactoryBean;
import com.hbase.repository.HbaseRepositoryInfo;
import com.hbase.repository.RowkeyGenerator;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * Created by leon on 2017/4/11.
 */
public class HtableScanHandler implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    
    private static final BiFunction<AnnotatedElement, Class<? extends Annotation>, Boolean> IS_ANNOTATED =
                                                                                            AnnotatedElement::isAnnotationPresent;
    
    private ClassLoader classLoader;

    public static final ConcurrentMap<Class, HbaseEntity> TABLE_CONTAINNER = new ConcurrentHashMap<>();

    static {
        EventMessage.getInstance().register(new ModePrepareListener());
    }
    
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        if (CustomEnvironmentListener.ENABLED){
            registerBean(importingClassMetadata, registry);
        }
    }

    private <T> void registerBean(AnnotationMetadata importingClassMetadata,
                                  BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes =
                AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(HbaseTableScan.class.getName()));
        String modelPackageName = attributes.getString("modelPackage");
        String repositoryPackageName = attributes.getString("repositoryPackage");

        Set<Class<T>> modelClasses = scanPackage(modelPackageName);
        Set<Class<T>> repositoryClasses =scanPackage(repositoryPackageName);
        Set<HbaseEntity> hbaseEntitySet = modelClasses.stream()
                                               .map(this::resolveModelClass)
                                               .filter((HbaseEntity entity) -> entity != null)
                                               .collect(Collectors.toSet());
        hbaseEntitySet.forEach(table -> TABLE_CONTAINNER.put(table.getJavaType(), table));
        Set<HbaseRepositoryInfo> repositorySet = repositoryClasses.stream()
                                                                  .map(this::resolveRepositoryClass)
                                                                  .filter((HbaseRepositoryInfo info) -> info !=null)
                                                                  .collect(Collectors.toSet());

        EventMessage.getInstance().publish(new ModelPrepareEvent(hbaseEntitySet));
        registerRepository(repositorySet, registry);
        System.out.printf("");
    }
    
    private void registerRepository(Set<HbaseRepositoryInfo> hbaseRepositoryInfos,
                                    BeanDefinitionRegistry registry) {
        for (HbaseRepositoryInfo info : hbaseRepositoryInfos) {
            BeanDefinitionBuilder beanDefinitionBuilder =
                                                        BeanDefinitionBuilder.rootBeanDefinition(HbaseRepositoryFactoryBean.class);
            beanDefinitionBuilder.addConstructorArgValue(info);
            AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            String beanName = info.getRepositoryClass().getSimpleName();
            beanName = beanName.replaceFirst(beanName.charAt(0) + "", (beanName.charAt(0) + "").toLowerCase());
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }
    
    private <T> Set<Class<T>> scanPackage(String packageName) {
        Optional.ofNullable(packageName)
                .filter(StringUtils::isNotBlank)
                .orElseThrow(() -> new ParseException(""));
        Set<Class<T>> classes = new LinkedHashSet<>();
        
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
    
    private <T, RK> HbaseEntity<T, RK> resolveModelClass(Class<T> clazz) {
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (!IS_ANNOTATED.apply(clazz, HbaseTable.class) || !IS_ANNOTATED.apply(clazz, RowKey.class)) {
            return null;
        }
        if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        Set<ModelElement> modelElements = ReflectManeger.getAllMethodField(clazz);
        Set<String> allFieldStringSet = modelElements.stream()
                                                     .map(element -> element.getField().getName())
                                                     .collect(Collectors.toSet());

        HbaseTable hbaseTable = clazz.getAnnotation(HbaseTable.class);
        String tableName = hbaseTable.name();
        RowKey rowKey = clazz.getAnnotation(RowKey.class);
        Set<String> configuredRowkeyColumnSet = new HashSet<>(Arrays.asList(rowKey.columnList()));
        
        Map<String, Class> rowkeyColumnMap = new HashMap<>();

        /* 校验配置的 rowkey 是否是真实存在的字段
        * 获取配置的 rowkey 的字段的类型
        * */
        for (String rowkeyColumn : configuredRowkeyColumnSet) {
            if (!allFieldStringSet.contains(rowkeyColumn)) {
                throw new ConfigurationException("找不到配置的 rowkey: " + rowkeyColumn);
            }
            for (ModelElement element : modelElements) {
                if (element.getField().getName().equals(rowkeyColumn)) {
                    Class fieldClass = element.getField().getType();
                    if (fieldClass == String.class || fieldClass == Long.class) {
                        rowkeyColumnMap.put(rowkeyColumn, element.getField().getClass());
                    }
                    else {
                        throw new ConfigurationException("rowkey 字段不支持该类型: " + fieldClass);
                    }
                }
            }
        }
        //获取所有配置的字段的名称，类型，family
        List<FamilyColumn> familyColumnList = new ArrayList<>();
        for (ModelElement element : modelElements){
            if (IS_ANNOTATED.apply(element.getReadMethod(), ColumnFamily.class)){
                FamilyColumn familyColumn = new FamilyColumn();
                familyColumn.setColumnName(element.getField().getName());
                familyColumn.setColumnType(element.getField().getType());
                familyColumn.setFamilyName(element.getReadMethod().getAnnotation(ColumnFamily.class).name());
                familyColumnList.add(familyColumn);
            }
        }

        return new MappingHbaseEntity<>(clazz, tableName, rowkeyColumnMap, familyColumnList);
    }

    
    private <T, R, RK> HbaseRepositoryInfo<T, R, RK> resolveRepositoryClass(Class<R> clazz) {
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (!IS_ANNOTATED.apply(clazz, com.hbase.annotation.HbaseRepository.class)) {
            return null;
        }
        HbaseRepositoryInfo<T, R, RK> hbaseRepositoryInfo = new HbaseRepositoryInfo<>();
        Type repositoryType = clazz.getGenericInterfaces()[0];
        if (!(repositoryType instanceof ParameterizedType
             && HbaseRepository.class == ((ParameterizedTypeImpl) repositoryType).getRawType())) {
            return null;
        }
        Type[] types = ((ParameterizedTypeImpl) repositoryType).getActualTypeArguments();
        for (int i = 0; i < types.length; i++) {
            Class typeClass = (Class) types[i];
            // 如果是 model class
            if (TABLE_CONTAINNER.containsKey(typeClass)) {
                HbaseEntity hbaseEntity = TABLE_CONTAINNER.get(typeClass);
                hbaseRepositoryInfo.setHbaseEntity(hbaseEntity);
                
                final Class finalRKClass = (Class) types[i == 0 ? 1 : 0];
                RowkeyGenerator<T, RK> rowkeyGenerator = new RowkeyGenerator<T, RK>() {
                    @Override
                    public RK getRowkey(T entity) {
                        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
                        // 字段为空的需要 去除掉
                        for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
                            if (entry.getValue() == null) {
                                entityMap.remove(entry.getKey());
                            }
                        }
                        if (finalRKClass == Long.class) {
                            Long rowkey = 0L;
                            // 生成 rowkey
                            Set<Map.Entry<String, Class>> entrySet = hbaseEntity.getRowkeyColumnMap().entrySet();
                            for (Map.Entry<String, Class> entry : entrySet) {
                                rowkey = rowkey + entityMap.get(entry.getKey()).hashCode();
                            }
                            return (RK) rowkey;
                        }
                        StringBuilder rowkey = new StringBuilder("");
                        Set<Map.Entry<String, Class>> entrySet = hbaseEntity.getRowkeyColumnMap().entrySet();
                        for (Map.Entry<String, Class> entry : entrySet) {
                            rowkey.append(entityMap.get(entry.getKey()).toString());
                        }
                        return (RK) rowkey;
                    }
                };
                ((MappingHbaseEntity<T, RK>) hbaseEntity).setRowkeyGenerator(rowkeyGenerator);
                TABLE_CONTAINNER.put(typeClass, hbaseEntity);
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
