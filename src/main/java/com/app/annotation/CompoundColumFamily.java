package com.app.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author yingyang
 * @date 2018/7/4.
 */
@Documented
@Inherited
@Target({ ElementType.TYPE })
@Retention(RUNTIME)
public @interface CompoundColumFamily {

    ColumnFamily[] columnFamily();

    boolean constraint() default false;
}
