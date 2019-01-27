package com.hbase.reflection;

import com.hbase.exception.ConfigurationException;
import com.sun.org.apache.regexp.internal.RE;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yang on 2019/1/27.
 */
public class ReflectManeger {
    
    private ReflectManeger() {
    }
    
    public static Set<Field> getAllField(Class clazz) {
        Set<Field> allFieldSet = new HashSet<>();
        Set<Method> allDeclareMethodSet = new HashSet<>();

        lookupAllSuperClassFields(clazz, allFieldSet);
        lookupAllSuperClassMethod(clazz, allDeclareMethodSet);
        
        Set<Method> allGetSetMethods = getAllMethod(clazz, allFieldSet);
        
        // 判断所有的 get set 方法是否都有
        for (Method method : allGetSetMethods) {
            if (!allDeclareMethodSet.contains(method)) {
                throw new ConfigurationException(clazz.getSimpleName() + " : " + method.getName());
            }
        }
        return allFieldSet;
    }
    
    private static void lookupAllSuperClassFields(Class clazz, Set<Field> fields) {
        fields.addAll(new HashSet<>(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()))));
        Class superClass = clazz.getSuperclass();
        while (superClass.getDeclaredFields().length > 0) {
            lookupAllSuperClassFields(superClass, fields);
        }
    }
    
    private static void lookupAllSuperClassMethod(Class clazz, Set<Method> allMethodSet) {
        allMethodSet.addAll(new HashSet<>(new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()))));
        Class superClass = clazz.getSuperclass();
        while (superClass.getDeclaredFields().length > 0) {
            lookupAllSuperClassMethod(superClass, allMethodSet);
        }
    }
    
    private static Set<Method> getAllMethod(Class clazz, Set<Field> fields) {
        Set<Method> allMethodSet = new HashSet<>();
        for (Field field : fields) {
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
                allMethodSet.add(propertyDescriptor.getReadMethod());
                allMethodSet.add(propertyDescriptor.getWriteMethod());
            }
            catch (IntrospectionException e) {
                e.printStackTrace();
                throw new ConfigurationException(clazz.getSimpleName() + " : " + field.getName());
            }
        }
        return allMethodSet;
    }
    
}
