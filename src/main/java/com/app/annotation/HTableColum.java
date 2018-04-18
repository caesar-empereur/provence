package com.app.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by leon on 2018/4/11.
 */
@Target({ METHOD, FIELD})
@Retention(RUNTIME)
public @interface HTableColum {
    String name() default "";
}
