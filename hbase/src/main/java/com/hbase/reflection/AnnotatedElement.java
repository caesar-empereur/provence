package com.hbase.reflection;

import java.lang.annotation.Annotation;

/**
 * Created by leon on 2018/4/18.
 */
public interface AnnotatedElement {

    /**
     * 获取成员变量或者方法中某种注解
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationType);

    /**
     * 判断是否被某种注解标注
     */
    <T extends Annotation> boolean isAnnotationPresent(Class<T> annotationType);

    /**
     * 获取成员变量或者方法中的全部注解
     */
    Annotation[] getAnnotations();

}
