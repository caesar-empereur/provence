package com.hbase.repository;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

import com.hbase.exception.ParseException;
import com.hbase.reflection.HbaseEntityInformation;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/1/15.
 */
public class HbaseRepositoryFactoryBean<R extends HbaseRepository<T, ID>, T, ID> implements FactoryBean<R> {
    
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
    
    public HbaseRepositoryFactoryBean(HbaseRepositoryInfo<T, R, ID> repositoryInfo) {
        Optional.ofNullable(repositoryInfo)
                .map(HbaseRepositoryInfo::getEntityInformation)
                .orElseThrow(() -> new ParseException("HbaseEntityInformation"));
        Optional.ofNullable(repositoryInfo.getRepositoryClass())
                .orElseThrow(() -> new ParseException("HbaseEntityInformation"));
        this.repositoryInterface = repositoryInfo.getRepositoryClass();
        this.repositorySupplier = () -> factorySupplier.get().getRepository(repositoryInterface,
                                                                            repositoryInfo.getEntityInformation());
    }
    
    class HbaseRepositoryFactory {
        
        private R getRepository(Class<R> repositoryInterface, HbaseEntityInformation<T, ID> entityInformation) {
            Optional.ofNullable(entityInformation)
                    .orElseThrow(() -> new ParseException("HbaseEntityInformation"));
            ProxyFactory proxyFactory = new ProxyFactory();
            SimpleHbaseRepository<T, ID> defaultHbaseCrudRepository =
                                                             new SimpleHbaseRepository<>(entityInformation);
            proxyFactory.setTarget(defaultHbaseCrudRepository);
            proxyFactory.setInterfaces(repositoryInterface);
            Object object = proxyFactory.getProxy(this.getClass().getClassLoader());
            return (R) object;
        }
    }
}
