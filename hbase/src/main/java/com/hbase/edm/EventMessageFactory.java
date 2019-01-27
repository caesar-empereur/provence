package com.hbase.edm;

/**
 * Created by YingYang on 2017/10/23.
 */
public class EventMessageFactory {
    
    private static EventMessage eventMessage;
    
    static {
        eventMessage = new EventMessageImpl();
    }
    
    public static EventMessage getInstance() {
        return eventMessage;
    }
}
