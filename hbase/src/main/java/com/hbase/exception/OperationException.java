package com.hbase.exception;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
public class OperationException extends RuntimeException {

    public OperationException(String message) {
        super(message);
    }
}
