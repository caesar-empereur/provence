package com.hbase.repository;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

import java.util.Optional;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/15.
 */
public class HbaseRepositoryFactoryBean<R extends HbaseCrudRepository> implements FactoryBean<R> {

    private Optional<Class<R>> repositoryInterfaceOptional = Optional.empty();

    @Nullable
    @Override
    public R getObject() throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory();
        DefaultHbaseCrudRepository defaultHbaseCrudRepository = DefaultHbaseCrudRepository.Builder.build();
        proxyFactory.setTarget(defaultHbaseCrudRepository);
        proxyFactory.setInterfaces(repositoryInterfaceOptional.get());
        Object object = proxyFactory.getProxy(this.getClass().getClassLoader());
        return (R) object;
    }
    
    @Nullable
    @Override
    public Class<?> getObjectType() {
        return repositoryInterfaceOptional.get();
    }
}
