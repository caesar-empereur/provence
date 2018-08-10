package com.hbase.pool.hikari;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/8/6.
 */
public enum EntryState {
                        NOT_IN_USE,
                        IN_USE,
                        REMOVED,
                        RESERVED;
}
