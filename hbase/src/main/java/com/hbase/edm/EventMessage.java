package com.hbase.edm;

import java.util.EventObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/28.
 */
@SuppressWarnings("all")
public class EventMessage {
    
    private static EventMessage eventMessage;
    
    public static EventMessage getInstance() {
        if (eventMessage == null) {
            synchronized (EventMessage.class) {
                if (eventMessage == null) {
                    eventMessage = new EventMessage();
                }
            }
        }
        return eventMessage;
    }
    
    private ConcurrentMap<Class<? extends EventObject>, EventListener> container =
                                                                                 new ConcurrentHashMap<>();
    
    private EventMessage() {
    }
    
    public void register(EventListener eventListener) {
        Class<EventObject> clazz =
                                 (Class<EventObject>) ((ParameterizedTypeImpl) eventListener.getClass()
                                                                                            .getGenericInterfaces()[0]).getActualTypeArguments()[0];
        container.put(clazz, eventListener);
    }
    
    public void publish(EventObject eventObject) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                emitter.onNext(eventObject);
            }
        }).subscribeOn(Schedulers.single())
          .observeOn(Schedulers.single())
          .subscribe(new Consumer<Object>() {
              @Override
              public void accept(Object o) throws Exception {
                  EventListener eventListener = container.get(eventObject.getClass());
                  if (eventListener == null) {
                      return;
                  }
                  eventListener.onEvent((EventObject) o);
              }
          }, new Consumer<Throwable>() {
              @Override
              public void accept(Throwable throwable) throws Exception {
                  throw new RuntimeException(throwable.getMessage());
              }
          });
    }

}
