package com.hbase.edm;

import java.util.Collection;
import java.util.EventObject;

import com.hbase.reflection.HbaseEntity;

/**
 * Created by yang on 2019/1/27.
 */
public class ModelPrepareEvent extends EventObject {
    
    public ModelPrepareEvent(Collection<HbaseEntity> htables) {
        super(htables);
    }
    
    @Override
    public Collection<HbaseEntity> getSource() {
        return (Collection<HbaseEntity>) super.getSource();
    }
}
