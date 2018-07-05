package com.hbase.reflection;

import java.util.Collection;

/**
 * Created by leon on 2018/4/18.
 */
public interface AnnotatedMember {

    AnnotatedClass getDeclaringClass();

    String getName();

    boolean isCollection();

    boolean isArray();

    Class<? extends Collection> getCollectionClass();
}
