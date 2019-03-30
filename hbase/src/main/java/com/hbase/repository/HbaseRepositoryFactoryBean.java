package com.hbase.repository;

import java.util.Optional;
import java.util.function.Supplier;

import com.hbase.reflection.HbaseEntity;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.lang.Nullable;

import com.hbase.exception.ParseException;

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
                .map(HbaseRepositoryInfo::getHbaseEntity)
                .orElseThrow(() -> new ParseException("HbaseEntity"));
        Optional.ofNullable(repositoryInfo.getRepositoryClass())
                .orElseThrow(() -> new ParseException("HbaseEntity"));
        this.repositoryInterface = repositoryInfo.getRepositoryClass();
        this.repositorySupplier = () -> factorySupplier.get().getRepository(repositoryInterface,
                                                                            repositoryInfo.getHbaseEntity());
    }
    
    class HbaseRepositoryFactory {
        
        private R getRepository(Class<R> repositoryInterface, HbaseEntity<T, ID> hbaseEntity) {
            Optional.ofNullable(hbaseEntity)
                    .orElseThrow(() -> new ParseException("HbaseEntity"));
            ProxyFactory proxyFactory = new ProxyFactory();
            SimpleHbaseRepository<T, ID> defaultHbaseCrudRepository =
                                                             new SimpleHbaseRepository<>(hbaseEntity);
            proxyFactory.setTarget(defaultHbaseCrudRepository);
            proxyFactory.setInterfaces(repositoryInterface);
            Object object = proxyFactory.getProxy(this.getClass().getClassLoader());
            return (R) object;
        }
    }
}
