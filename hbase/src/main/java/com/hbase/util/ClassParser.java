package com.hbase.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/21.
 */
public class ClassParser {
    
    private static final String PACK_SEPARATOR = ".";
    
    private static final String ENCODE = "UTF-8";
    
    private static final String CLASS = "class";
    
    private static final String FILE = "file";
    
    public static Set<Class> scanPackage(String packageName) {
        Set<Class> clazzs = new HashSet<>();
        String packageDirName = packageName.replace(PACK_SEPARATOR, File.separator);
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if (FILE.equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), ENCODE);
                    resolveClass(packageName, filePath, clazzs);
                }
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return clazzs;
    }
    
    private static void resolveClass(String packageName, String filePath, Set<Class> clazzs) {
        File dir = new File(filePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] dirFiles = dir.listFiles((File file) -> {
            boolean acceptDir = file.isDirectory();
            boolean acceptClass = file.getName().endsWith(CLASS);
            return acceptDir || acceptClass;
        });
        if (dirFiles == null || dirFiles.length == 0) {
            return;
        }
        for (File file : dirFiles) {
            if (file.isDirectory()) {
                resolveClass(packageName + PACK_SEPARATOR + file.getName(),
                             file.getAbsolutePath(),
                             clazzs);
            }
            else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    clazzs.add(Thread.currentThread()
                                     .getContextClassLoader()
                                     .loadClass(packageName + PACK_SEPARATOR + className));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
