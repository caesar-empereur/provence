package com.hbase.edm;

import java.lang.reflect.ParameterizedType;
import java.util.EventObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2018/5/22.
 */
public final class EventMessageImpl implements EventMessage<EventObject, AbstractEventListener> {
    
    private static final ConcurrentMap<Class, AbstractEventListener> EVENT_TO_LISTENER_MAPPINGS =
                                                                                                new ConcurrentHashMap<>(20);
    
    @Override
    public void register(EventObject eventObject, AbstractEventListener eventListener) {
        Class<?> clazz =
                       (Class<?>) ((ParameterizedType) eventListener.getClass()
                                                                    .getGenericSuperclass()).getActualTypeArguments()[0];
        EVENT_TO_LISTENER_MAPPINGS.put(clazz, eventListener);
    }

    @Override
    public void publish(EventObject eventObject) {
        Observable.create((ObservableEmitter<Object> emitter) -> emitter.onNext(eventObject))
                  .subscribeOn(Schedulers.io())
                  .observeOn(Schedulers.io())
                  .subscribe((Object o) -> {
                      EventListener eventListener =
                                                  EVENT_TO_LISTENER_MAPPINGS.get(eventObject.getClass());
                      if (eventListener == null) {
                          return;
                      }
                      eventListener.onEvent((EventObject) o);
                  });
    }
    
}
