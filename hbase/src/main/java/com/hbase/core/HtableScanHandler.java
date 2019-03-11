package com.hbase.core;

import com.alibaba.fastjson.JSON;
import com.hbase.annotation.*;
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
import org.springframework.data.repository.config.RepositoryBeanNameGenerator;
import org.springframework.util.ClassUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

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

/**
 * Created by leon on 2017/4/11.
 */
public class HtableScanHandler implements ImportBeanDefinitionRegistrar, ResourceLoaderAware{

    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private static final Function<Class, Set<Field>> FIELD_RESOLVER = ReflectManeger::getAllField;

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    
    private Supplier<RepositoryBeanNameGenerator> beanNameGenerator =
                                                                    () -> new RepositoryBeanNameGenerator(this.getClass()
                                                                                                              .getClassLoader());
    
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
        registerBean(importingClassMetadata, registry);
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
//            String beanName = beanNameGenerator.get().generateBeanName(beanDefinition);
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
        if (!IS_ANNOTATED.apply(clazz, HbaseTable.class) || !IS_ANNOTATED.apply(clazz, RowKey.class)
            || !IS_ANNOTATED.apply(clazz, CompoundColumFamily.class)) {
            return null;
        }
        if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        Set<Field> allFieldSet = FIELD_RESOLVER.apply(clazz);
        Map<String, Class> allFieldMap = new HashMap<>();
        Set<String> allFieldStringSet = allFieldSet.stream().map(field -> {
            allFieldMap.put(field.getName(), field.getType());
            return field.getName();
        }).collect(Collectors.toSet());
        
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
            for (Field field : allFieldSet) {
                if (field.getName().equals(rowkeyColumn)) {
                    Class fieldClass = field.getType();
                    if (fieldClass == String.class || fieldClass == Long.class) {
                        rowkeyColumnMap.put(rowkeyColumn, field.getClass());
                    }
                    else {
                        throw new ConfigurationException("rowkey 字段不支持该类型: " + fieldClass);
                    }
                }
            }
        }
        //获取所有配置的字段的名称，类型，family
        List<FamilyColumn> familyColumnList = new ArrayList<>();
        CompoundColumFamily compoundColumFamily = clazz.getAnnotation(CompoundColumFamily.class);
        List<String> columnListConfiged = new ArrayList<>();
        for (ColumnFamily columnFamily : compoundColumFamily.columnFamily()){
            for (String column : columnFamily.columnList()){
                FamilyColumn familyColumn = new FamilyColumn();
                familyColumn.setColumnName(column);
                familyColumn.setFamilyName(columnFamily.name());
                familyColumn.setColumnType(Optional.of(allFieldMap.get(column)).get());
                familyColumnList.add(familyColumn);
                columnListConfiged.add(column);
            }
        }
        if (columnListConfiged.size() != new HashSet<>(columnListConfiged).size()) {
            throw new ConfigurationException("不同 ColumnFamily 不能包含相同 column");
        }

        return new MappingHbaseEntity<>(clazz, tableName, rowkeyColumnMap, familyColumnList);
    }

    
    private <T, R, RK> HbaseRepositoryInfo<T, R, RK> resolveRepositoryClass(Class<R> clazz) {
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (!IS_ANNOTATED.apply(clazz, com.hbase.annotation.HbaseRepository.class) ) {
            return null;
        }
        HbaseRepositoryInfo<T, R, RK> hbaseRepositoryInfo = new HbaseRepositoryInfo<>();
        for (Type repositoryType : clazz.getGenericInterfaces()) {
            if (repositoryType instanceof ParameterizedType
                && HbaseRepository.class == ((ParameterizedTypeImpl) repositoryType).getRawType()) {

                Type[] types = ((ParameterizedTypeImpl) repositoryType).getActualTypeArguments();

                for (int i = 0; i < types.length; i++) {
                    Class typeClass = (Class) types[i];
                    //如果是 model class
                    if (TABLE_CONTAINNER.containsKey(typeClass)) {
                        HbaseEntity hbaseEntity = TABLE_CONTAINNER.get(typeClass);
                        hbaseRepositoryInfo.setHbaseEntity(hbaseEntity);

                        final Class finalRKClass = (Class) types[i == 0 ? 1 : 0];
                        RowkeyGenerator<T, RK> rowkeyGenerator = new RowkeyGenerator<T, RK>() {
                            @Override
                            public RK getRowkey(T entity) {
                                Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
                                //字段为空的需要 去除掉
                                for (Map.Entry<String, Object> entry : entityMap.entrySet()){
                                    if (entry.getValue() == null){
                                        entityMap.remove(entry);
                                    }
                                }
                                if (finalRKClass == Long.class){
                                    Long rowkey = 0l;
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
                        ((MappingHbaseEntity<T,RK>) hbaseEntity).setRowkeyGenerator(rowkeyGenerator);
                        TABLE_CONTAINNER.put(typeClass, hbaseEntity);
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
