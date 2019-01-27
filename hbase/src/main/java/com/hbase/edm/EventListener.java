package com.hbase.edm;

import java.util.EventObject;

/**
 * Created by YingYang on 2017/10/23.
 */
public interface EventListener<T extends EventObject> {
    
    void onEvent(T event);
    
    default void checkEvent(T event) {
        if (event == null || event.getSource() == null) {
            return;
        }
    }
}
