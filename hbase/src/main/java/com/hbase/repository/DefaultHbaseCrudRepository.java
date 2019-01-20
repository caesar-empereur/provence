package com.hbase.repository;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hbase.exception.OperationException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

import com.alibaba.fastjson.JSON;
import com.hbase.core.Htable;
import com.hbase.core.HtableScanHandler;
import com.hbase.exception.ConnectionException;
import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.aop.framework.ProxyFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class DefaultHbaseCrudRepository implements HbaseCrudRepository {
    
    private ConnectionProvider connectionProvider = ConnectionPoolManager.getInstance();
    
    @Override
    public Object save(Object model) {
        Htable htable = HtableScanHandler.TABLE_CONTAINNER.get(model.getClass());
        Connection connection = connectionProvider.getConnection();
        Table table;
        try {
            table = connection.getTable(TableName.valueOf(htable.getTableName().get()));
        }
        catch (IOException e) {
            throw new ConnectionException("获取不到表: " + htable.getTableName()
                                          + "\n"
                                          + e.getMessage());
        }
        try {
            table.put(convertModelToPut(model));
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        connectionProvider.recycleConnection(connection);
        return model;
    }

    private List<Put> convertModelListToPut(Collection<Object> models){
        List<Put> putList = new ArrayList<>();
        models.forEach(model -> putList.add(convertModelToPut(model)));
        return putList;
    }

    private Put convertModelToPut(Object model){
        Htable htable = HtableScanHandler.TABLE_CONTAINNER.get(model.getClass());
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(model), Map.class);
        //字段为空的需要 去除掉
        for (Map.Entry<String, Object> entry : entityMap.entrySet()){
            if (entry.getValue() == null){
                entityMap.remove(entry.getKey());
            }
        }
        int rowkey = 0;
        // 生成 rowkey
        for (Map.Entry<String, Class> entry : htable.getRowKeyColumns().get().entrySet()) {
            rowkey = rowkey + entityMap.get(entry.getKey()).hashCode();
        }
        Put put = new Put(Bytes.toBytes(rowkey));
        // 获取到各个字段的值
        for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
            put.addColumn(entry.getKey().getBytes(), null, JSON.toJSONString(entry.getValue()).getBytes());
        }
        return put;
    }
    
    @Override
    public Collection saveAll(Collection models) {
        Htable htable = HtableScanHandler.TABLE_CONTAINNER.get(new ArrayList<>(models).get(0).getClass());
        Connection connection = connectionProvider.getConnection();
        Table table;
        try {
            table = connection.getTable(TableName.valueOf(htable.getTableName().get()));
        }
        catch (IOException e) {
            throw new ConnectionException("获取不到表: " + htable.getTableName()
                                          + "\n"
                                          + e.getMessage());
        }
        try {
            table.put(convertModelListToPut(models));
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        connectionProvider.recycleConnection(connection);
        return null;
    }
    
    @Override
    public void delete(Object model) {
        
    }
    
    @Override
    public void deleteAll() {
        
    }
    
    @Override
    public void deleteAll(Collection models) {
        
    }
    
    @Override
    public long count() {
        String tableName = null;
        for (Type repositoryType : this.getClass().getGenericInterfaces()) {
            if (repositoryType instanceof ParameterizedType
                && HbaseCrudRepository.class == ((ParameterizedTypeImpl) repositoryType).getRawType()) {
                for (Type modelType : ((ParameterizedTypeImpl) repositoryType).getActualTypeArguments()) {
                    Class modelClass = (Class) modelType;
                    Htable htable = HtableScanHandler.TABLE_CONTAINNER.get(modelClass);
                    if (htable != null) {
                        tableName = htable.getTableName().get();
                    }
                }
            }
        }
        Connection connection = connectionProvider.getConnection();
        Table table;
        try {
            table = connection.getTable(TableName.valueOf(tableName));
        }
        catch (IOException e) {
            throw new ConnectionException("获取不到表: " + tableName + "\n" + e.getMessage());
        }
        Scan scan = new Scan();
        scan.setFilter(new FirstKeyOnlyFilter());
        long rowCount = 0;
        try {
            ResultScanner resultScanner = table.getScanner(scan);
            for (Result result : resultScanner) {
                rowCount += result.size();
            }
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        connectionProvider.recycleConnection(connection);
        return rowCount;
    }
    
    @Override
    public Collection findByRowKeys(Collection rowkeys) {
        return null;
    }
    
    @Override
    public Object findByRowkey(Object rowkey) {
        return null;
    }
    
//    private DefaultHbaseCrudRepository() {
//    }
    
    public static class Builder {
        public static DefaultHbaseCrudRepository build() {
            return new DefaultHbaseCrudRepository();
        }
    }
    
}
