package com.hbase.edm;

import java.lang.reflect.ParameterizedType;
import java.util.EventObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by YingYang on 2017/10/23.
 */
@SuppressWarnings("all")
public abstract class AbstractEventListener<E extends EventObject> implements
                                           EventListener<E>,
                                           InitializingBean {
    protected static final Log log =
                                   LogFactory.getLog(AbstractEventListener.class);

    private volatile E t;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        EventMessageFactory.getInstance().register(t, this);
    }
    
    protected String getEventName() {
        Class<E> clazz =
                       (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return clazz.getSimpleName();
    }
}
