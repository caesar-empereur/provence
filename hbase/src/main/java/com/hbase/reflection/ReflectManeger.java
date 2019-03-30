package com.hbase.reflection;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hbase.exception.ConfigurationException;

/**
 * Created by yang on 2019/1/27.
 */
public class ReflectManeger {
    
    private ReflectManeger() {
    }
    
    // 获取所有的 get set 方法 field
    public static Set<ModelElement> getAllMethodField(Class clazz) {
        Set<Field> allFieldSet = new HashSet<>();
        lookupAllSuperClassFields(clazz, allFieldSet);
        Set<ModelElement> modelElements = new HashSet<>();
        for (Field field : allFieldSet) {
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                                                                               clazz);
                ModelElement modelElement = new ModelElement(field,
                                                             propertyDescriptor.getReadMethod(),
                                                             propertyDescriptor.getWriteMethod());
                modelElements.add(modelElement);
            }
            catch (IntrospectionException e) {
                throw new ConfigurationException(e.getMessage());
            }
        }
        return modelElements;
    }
    
    private static void lookupAllSuperClassFields(Class clazz, Set<Field> fields) {
        fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
        Class superClass = clazz.getSuperclass();
        if (superClass.getDeclaredFields().length == 0) {
            return;
        }
        lookupAllSuperClassFields(superClass, fields);
    }
    
}
