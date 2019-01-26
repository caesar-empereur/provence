package com.hbase.reflection;

/**
 * Created by leon on 2018/4/18.
 */
public interface AnnotatedClass extends AnnotatedElement {
    
    String getClassName();
    
    AnnotatedClass getSuperclass();
    
    AnnotatedClass[] getInterfaces();
    
    boolean isInterface();
    
    boolean isAbstract();
    
    boolean isPrimitive();
    
    boolean isEnum();
    
    boolean isAssignableFrom(AnnotatedClass c);
    
}
