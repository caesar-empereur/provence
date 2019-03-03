package com.hbase.core;

import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.HbaseTableScan;
import com.hbase.annotation.RowKey;
import com.hbase.edm.EventMessage;
import com.hbase.edm.ModePrepareListener;
import com.hbase.edm.ModelPrepareEvent;
import com.hbase.exception.ConfigurationException;
import com.hbase.exception.ParseException;
import com.hbase.reflection.HbaseEntityInformation;
import com.hbase.reflection.MappingHbaseEntityInformation;
import com.hbase.reflection.ReflectManeger;
import com.hbase.repository.HbaseRepository;
import com.hbase.repository.HbaseRepositoryFactoryBean;
import com.hbase.repository.HbaseRepositoryInfo;
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

    public  static final ConcurrentMap<Class, HbaseEntityInformation> TABLE_CONTAINNER = new ConcurrentHashMap<>();

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
        Set<HbaseEntityInformation> htables = modelClasses.stream()
                                          .map(this::resolveModelClass)
                                          .filter((HbaseEntityInformation entity) -> entity != null)
                                          .collect(Collectors.toSet());
        htables.forEach(table -> TABLE_CONTAINNER.put(table.getJavaType(), table));
        Set<HbaseRepositoryInfo> repositorySet = repositoryClasses.stream()
                                                                  .map(this::resolveRepositoryClass)
                                                                  .filter((HbaseRepositoryInfo info) -> info !=null)
                                                                  .collect(Collectors.toSet());

        EventMessage.getInstance().publish(new ModelPrepareEvent(htables));
        registerRepository(repositorySet, registry);
    }
    
    private void registerRepository(Set<HbaseRepositoryInfo> hbaseRepositoryInfos,
                                    BeanDefinitionRegistry registry) {
        for (HbaseRepositoryInfo info : hbaseRepositoryInfos) {
            BeanDefinitionBuilder beanDefinitionBuilder =
                                                        BeanDefinitionBuilder.rootBeanDefinition(HbaseRepositoryFactoryBean.class);
            beanDefinitionBuilder.addConstructorArgValue(info);
            AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            String beanName = info.getRepositoryClass().getSimpleName().replace("A","a");
//            String beanName = beanNameGenerator.get().generateBeanName(beanDefinition);

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
    
    private <T, ID> HbaseEntityInformation<T, ID> resolveModelClass(Class<T> clazz) {
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (!IS_ANNOTATED.apply(clazz, HbaseTable.class) || !IS_ANNOTATED.apply(clazz, RowKey.class)) {
            return null;
        }
        if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return null;
        }
        Set<Field> allFieldSet = FIELD_RESOLVER.apply(clazz);

        Set<String> allFieldStringSet = allFieldSet.stream()
                                                   .map(Field::getName)
                                                   .collect(Collectors.toSet());
        
        HbaseTable hbaseTable = clazz.getAnnotation(HbaseTable.class);
        String tableName = hbaseTable.name();
        Class<ID> idClass = null;
        RowKey rowKey = clazz.getAnnotation(RowKey.class);
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
        for (Field field : allFieldSet){
            if (field.getName().equals(hbaseTable.id())){
                idClass =(Class<ID>) field.getType();
            }
        }
        return new MappingHbaseEntityInformation<>(clazz, tableName, idClass, rowkeyColumnMap);
    }
    

    
    private <T, R, ID> HbaseRepositoryInfo<T, R, ID> resolveRepositoryClass(Class<R> clazz) {
        Optional.ofNullable(clazz).orElseThrow(() -> new ParseException(""));
        if (!IS_ANNOTATED.apply(clazz, com.hbase.annotation.HbaseRepository.class) ) {
            return null;
        }
        HbaseRepositoryInfo<T, R, ID> hbaseRepositoryInfo = new HbaseRepositoryInfo<>();
        for (Type repositoryType : clazz.getGenericInterfaces()) {
            if (repositoryType instanceof ParameterizedType
                && HbaseRepository.class == ((ParameterizedTypeImpl) repositoryType).getRawType()) {
                for (Type modelType : ((ParameterizedTypeImpl) repositoryType).getActualTypeArguments()) {
                    Class modelClass = (Class) modelType;
                    if (TABLE_CONTAINNER.containsKey(modelClass)) {
                        hbaseRepositoryInfo.setEntityInformation(TABLE_CONTAINNER.get(modelClass));
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
