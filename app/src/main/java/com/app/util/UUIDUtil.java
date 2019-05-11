package com.app.util;

import java.util.UUID;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/5/4.
 */
public class UUIDUtil {
    
    public static String randomUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
