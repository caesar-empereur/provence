package com.hbase.repository;

import java.util.function.Supplier;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/15.
 */
public class HbaseRepositoryFactoryBean<R extends HbaseCrudRepository> implements FactoryBean<R> {
    
    private Class<R> repositoryInterface;
    
    private Supplier<R> repositorySupplier;
    
    private Supplier<HbaseRepositoryFactory> factorySupplier = HbaseRepositoryFactory::new;
    
    @Nullable
    @Override
    public R getObject() throws Exception {
        return this.repositorySupplier.get();
    }
    
    @Nullable
    @Override
    public Class<R> getObjectType() {
        return repositoryInterface;
    }
    
    public HbaseRepositoryFactoryBean(Class<R> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
        this.repositorySupplier = () -> factorySupplier.get().getRepository(repositoryInterface);
    }
    
    private class HbaseRepositoryFactory {
        
        private R getRepository(Class<R> repositoryInterface) {
            ProxyFactory proxyFactory = new ProxyFactory();
            DefaultHbaseCrudRepository defaultHbaseCrudRepository =
                                                                  DefaultHbaseCrudRepository.Builder.build();
            proxyFactory.setTarget(defaultHbaseCrudRepository);
            proxyFactory.setInterfaces(repositoryInterface);
            Object object = proxyFactory.getProxy(this.getClass().getClassLoader());
            return (R) object;
        }
    }
}
