package com.hbase.edm;

/**
 * Created by YingYang on 2017/10/23.
 */
public interface EventMessage<E, L extends EventListener> {
    
    void register(E eventObject, L eventListener);
    
    void publish(E eventObject);
}
