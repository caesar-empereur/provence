package com.hbase.exception;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/5.
 */
public class ConnectionException extends RuntimeException {

    public ConnectionException(String message) {
        super(message);
    }
}
