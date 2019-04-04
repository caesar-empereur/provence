package com.hbase.reflection;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/4/1.
 */
@Data
public class RowkeyInfo implements Comparable<RowkeyInfo> {
    
    private Field field;
    
    private int order;

    @Override
    public int compareTo(RowkeyInfo o) {
        if (this.order == o.order) {
            return 0;
        }
        return this.order > o.order ? 1 : -1;
    }
}
