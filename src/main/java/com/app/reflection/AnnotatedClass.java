package com.app.reflection;

import java.util.List;

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

    List<AnnotatedProperty>getDeclaredProperties(String accessType);

    List<AnnotatedMethod> getDeclaredMethods();
}
