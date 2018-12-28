package com.hbase.exception;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/18.
 */
public class InitException extends RuntimeException {
    
    public InitException(String message) {
        super("表结构初始异常: " + message);
    }
}
