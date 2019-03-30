package com.hbase.reflection;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/30.
 */
@Data
public class ModelElement {

    private Field field;

    private Method readMethod;

    private Method writeMethod;

    public ModelElement(Field field, Method readMethod, Method writeMethod) {
        this.field = field;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
    }
}
