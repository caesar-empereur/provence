package com.hbase.edm;

import java.util.EventObject;

/**
 * Created by YingYang on 2017/10/23.
 */
public interface EventListener<E extends EventObject>{
    
    void onEvent(E eventObject);

}
