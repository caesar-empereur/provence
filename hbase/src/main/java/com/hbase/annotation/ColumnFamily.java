package com.hbase.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by leon on 2018/4/11.
 */
@Documented
@Inherited
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ColumnFamily {

    String name();

    boolean unique() default false;
}
