package com.app.core;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author yingyang
 * @date 2018/7/2.
 */
public class PeersistencePackageScanManager {
    private static final String CLASS_RESOURCE_PATTERN = "/**/*.class";
    
    private ResourcePatternResolver resourcePatternResolver =
                                                            new PathMatchingResourcePatternResolver();
    
    public Set<String> scanPackage(String packageName) throws Exception {
        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                         + ClassUtils.convertClassNameToResourcePath(packageName)
                         + CLASS_RESOURCE_PATTERN;
        Resource[] resources = this.resourcePatternResolver.getResources(pattern);
        MetadataReaderFactory readerFactory =
                                            new CachingMetadataReaderFactory(this.resourcePatternResolver);
        
        Set<String> classNames = new LinkedHashSet<>();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                classNames.add(className);
            }
        }
        return classNames;
    }
}
