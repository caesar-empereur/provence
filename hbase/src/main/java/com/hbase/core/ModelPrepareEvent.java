package com.hbase.core;

import java.util.Collection;
import java.util.EventObject;

/**
 * Created by yang on 2019/1/27.
 */
public class ModelPrepareEvent extends EventObject {
    
    public ModelPrepareEvent(Collection<Htable> htables) {
        super(htables);
    }
    
    @Override
    public Collection<Htable> getSource() {
        return (Collection<Htable>) super.getSource();
    }
}
