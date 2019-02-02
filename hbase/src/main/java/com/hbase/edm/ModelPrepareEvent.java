package com.hbase.edm;

import java.util.Collection;
import java.util.EventObject;

import com.hbase.reflection.HbaseEntityInformation;

/**
 * Created by yang on 2019/1/27.
 */
public class ModelPrepareEvent extends EventObject {
    
    public ModelPrepareEvent(Collection<HbaseEntityInformation> htables) {
        super(htables);
    }
    
    @Override
    public Collection<HbaseEntityInformation> getSource() {
        return (Collection<HbaseEntityInformation>) super.getSource();
    }
}
