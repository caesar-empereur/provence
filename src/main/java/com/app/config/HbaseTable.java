package com.app.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by leon on 2018/4/11.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface HbaseTable {

    String name() default "";
}
