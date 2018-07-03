package com.app.core;

import com.app.annotation.HbaseTable;
import com.app.annotation.HbaseTableScan;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
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
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        classes.forEach(clazz -> {
            clazz.getAnnotations();
        });
        return htables;
    }
}
