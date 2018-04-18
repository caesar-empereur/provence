package com.app.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by leon on 2017/4/11.
 */
@Configuration
public class HbaseTableScanHandler {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    private AnnotationAttributes annotationAttributes;
    
    private Set<String> packageNames;
    
    private static final String BEAN = HbaseTableScanHandler.class.getName();
    
    private static final HbaseTableScanHandler NONE =
                                                    new HbaseTableScanHandler();
    
    private static String[] addPackageNames(ConstructorArgumentValues constructorArguments,
                                            Collection<String> packageNames) {
        String[] existing =
                          (String[]) constructorArguments.getIndexedArgumentValue(0,
                                                                                  String[].class)
                                                         .getValue();
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(Arrays.asList(existing));
        merged.addAll(packageNames);
        return merged.toArray(new String[merged.size()]);
    }
    
    private static void register(BeanDefinitionRegistry registry,
                                 Collection<String> packageNames) {
        Assert.notNull(registry, "Registry must not be null");
        Assert.notNull(packageNames, "PackageNames must not be null");
        if (registry.containsBeanDefinition(BEAN)) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);
            ConstructorArgumentValues constructorArguments =
                                                           beanDefinition.getConstructorArgumentValues();
            constructorArguments.addIndexedArgumentValue(0,
                                                         addPackageNames(constructorArguments,
                                                                         packageNames));
        }
        else {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(EntityScanPackages.class);
            beanDefinition.getConstructorArgumentValues()
                          .addIndexedArgumentValue(0,
                                                   packageNames.toArray(new String[packageNames.size()]));
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(BEAN, beanDefinition);
        }
    }
    
    static class Registrar implements ImportBeanDefinitionRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            
        }
        
        private Set<String> getPackagesToScan(AnnotationMetadata metadata) {
            AnnotationAttributes attributes =
                                            AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(HbaseTable.class.getName()));
            String[] basePackages = attributes.getStringArray("basePackages");
            Class<?>[] basePackageClasses =
                                          attributes.getClassArray("basePackageClasses");
            Set<String> packagesToScan = new LinkedHashSet<>();
            packagesToScan.addAll(Arrays.asList(basePackages));
            for (Class<?> basePackageClass : basePackageClasses) {
                packagesToScan.add(ClassUtils.getPackageName(basePackageClass));
            }
            if (packagesToScan.isEmpty()) {
                String packageName =
                                   ClassUtils.getPackageName(metadata.getClassName());
                Assert.state(!StringUtils.isEmpty(packageName),
                             "@HbaseTableScan cannot be used with the default package");
                return Collections.singleton(packageName);
            }
            return packagesToScan;
        }
    }
}
