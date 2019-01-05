package com.hbase.repository;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.hbase.exception.OperationException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.alibaba.fastjson.JSON;
import com.hbase.core.Htable;
import com.hbase.core.HtableScanHandler;
import com.hbase.exception.ConnectionException;
import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;

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
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(model), Map.class);
        
        Connection connection = connectionProvider.getConnection();
        Table table;
        try {
            table = connection.getTable(TableName.valueOf(htable.getTableName()));
        }
        catch (IOException e) {
            throw new ConnectionException("获取不到表: " + htable.getTableName()
                                          + "\n"
                                          + e.getMessage());
        }
        String rowkey = null;
        // 生成 rowkey
        for (Map.Entry<String, Class> entry : htable.getRowKeyColumns().entrySet()) {
            rowkey = rowkey + entityMap.get(entry.getKey());
        }
        Put put = new Put(rowkey.getBytes());
        // 获取到各个字段的值
        for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
            put.addColumn(entry.getKey().getBytes(),
                          null,
                          JSON.toJSONString(entry.getValue()).getBytes());
        }
        try {
            table.put(put);
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        connectionProvider.recycleConnection(connection);
        return model;
    }
    
    @Override
    public Collection saveAll(Collection models) {
        return null;
    }
    
    @Override
    public void deleteByRowkey(Object rowkey) {
        
    }
    
    @Override
    public void deleteAll() {
        
    }
    
    @Override
    public void deleteAll(Collection rowkeys) {
        
    }
    
    @Override
    public long count() {
        return 0;
    }
    
    @Override
    public Collection findByRowKeys(Collection rowkeys) {
        return null;
    }
    
    @Override
    public Object findByRowkey(Object rowkey) {
        return null;
    }
    
    private DefaultHbaseCrudRepository() {
    }
    
    public static class Builder {
        public static DefaultHbaseCrudRepository build() {
            return new DefaultHbaseCrudRepository();
        }
    }
    
}
