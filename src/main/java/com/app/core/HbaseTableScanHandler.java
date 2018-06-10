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
import org.springframework.core.type.AnnotationMetadata;
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
    
    private AnnotationAttributes annotationAttributes;
    
    private Set<String> packageNames;
    
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
            Set<Class<?>> classSet = getClasses(packageName);
            for (Class<?> clazz : classSet) {
                log.info(clazz.getSimpleName());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    
    private static Set<Class<?>> getClasses(String packageName) throws IOException,
                                                                ClassNotFoundException {
        
        Set<Class<?>> classes = new HashSet<>();
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs = Thread.currentThread()
                                      .getContextClassLoader()
                                      .getResources(packageDirName);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();
            String protocol = url.getProtocol();
            if ("file".equals(protocol)) {
                String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                findAndAddClassesInPackageByFile(packageName, filePath, classes);
            }
//            else if ("jar".equals(protocol)) {
//                JarFile jar;
//                jar = ((JarURLConnection) url.openConnection()).getJarFile();
//                Enumeration<JarEntry> entries = jar.entries();
//                while (entries.hasMoreElements()) {
//                    JarEntry entry = entries.nextElement();
//                    String name = entry.getName();
//                    if (name.charAt(0) == '/') {
//                        name = name.substring(1);
//                    }
//                    if (name.startsWith(packageDirName)) {
//                        int idx = name.lastIndexOf('/');
//                        if (idx != -1) {
//                            packageName = name.substring(0, idx).replace('/', '.');
//                        }
//                        if ((idx != -1)) {
//                            if (name.endsWith(".class") && !entry.isDirectory()) {
//                                String className = name.substring(packageName.length() + 1,
//                                                                  name.length() - 6);
//                                classes.add(Class.forName(packageName + '.' + className));
//                            }
//                        }
//                    }
//                }
//            }
        }
        
        return classes;
    }
    
    private static void findAndAddClassesInPackageByFile(String packageName,
                                                         String packagePath,
                                                         Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirfiles = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                                                 file.getAbsolutePath(),
                                                 classes);
            }
            else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    classes.add(Class.forName(packageName + '.' + className));
                }
                catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
