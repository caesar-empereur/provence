package com.app.reflection;

import org.hibernate.annotations.common.reflection.ClassLoaderDelegate;

import java.lang.reflect.Method;

/**
 * Created by leon on 2018/4/18.
 */
public interface AnnotationReflectionManager {
    
    void injectClassLoaderDelegate(ClassLoaderDelegate delegate);
    
    ClassLoaderDelegate getClassLoaderDelegate();
    
    <T> AnnotatedClass toAnnotatedClass(Class<T> clazz);
    
    Class toClass(AnnotatedClass annotatedClass);
    
    Method toMethod(AnnotatedMethod annotatedMethod);

    AnnotatedClass classForName(String name);
}
