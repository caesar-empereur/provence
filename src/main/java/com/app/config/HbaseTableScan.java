package com.app.config;

import java.lang.annotation.*;

/**
 * Created by leon on 2018/4/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HbaseTableScan {
}
