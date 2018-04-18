package com.app.config;

import org.springframework.core.annotation.AliasFor;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by leon on 2018/4/11.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface HbaseTable {

    @AliasFor("basePackages")
    String name() default "";
}
