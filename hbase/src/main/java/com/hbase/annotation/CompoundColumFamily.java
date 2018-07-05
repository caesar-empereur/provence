package com.hbase.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

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
