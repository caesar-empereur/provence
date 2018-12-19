package com.hbase.annotation;

import java.lang.annotation.*;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HbaseRepository {
}
