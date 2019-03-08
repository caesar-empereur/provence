package com.hbase.repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.alibaba.fastjson.JSON;
import com.hbase.core.FamilyColumn;
import com.hbase.exception.ConnectionException;
import com.hbase.exception.OperationException;
import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;
import com.hbase.reflection.HbaseEntity;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class SimpleHbaseRepository<T, ID> implements HbaseRepository<T, ID> {

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    private ConnectionProvider<Connection> connectionProvider = ConnectionPoolManager.getInstance();

    private HbaseEntity<T, ID> hbaseEntity;

    private Map<String, FamilyColumn> familyColumnMap;

    public SimpleHbaseRepository(HbaseEntity<T, ID> hbaseEntity) {
        this.hbaseEntity = Optional.of(hbaseEntity).get();
        familyColumnMap = new HashMap<>();
        for (FamilyColumn familyColumn : hbaseEntity.getFamilyColumnList()){
            familyColumnMap.put(familyColumn.getColumnName(), familyColumn);
        }
    }

    private int getRowkey(T entity){
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        //字段为空的需要 去除掉
        for (Map.Entry<String, Object> entry : entityMap.entrySet()){
            if (entry.getValue() == null){
                entityMap.remove(entry);
            }
        }
        int rowkey = 0;
        // 生成 rowkey
        Set<Map.Entry<String, Class>> entrySet = hbaseEntity.getRowkeyColumns().entrySet();
        for (Map.Entry<String, Class> entry : entrySet) {
            rowkey = rowkey + entityMap.get(entry.getKey()).hashCode();
        }
        return rowkey;
    }

    @Override
    public <S extends T> S save(S entity) {
        doSaveOperation(Arrays.asList(toPut(entity)));
        return entity;
    }

    private <S extends T> Put toPut(S entity){
        Put put = new Put(Bytes.toBytes(getRowkey(entity)));
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        for (Map.Entry<String, Object> objectFieldValue : entityMap.entrySet()){
            if (objectFieldValue.getValue() == null){
                entityMap.remove(objectFieldValue);
            }
            put.addColumn(familyColumnMap.get(objectFieldValue.getKey()).getFamilyName().getBytes(),
                          objectFieldValue.getKey().getBytes(),
                          convertToByte(objectFieldValue.getValue()));
        }
        return put;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<Put> putList = new ArrayList<>();
        entities.forEach(model -> putList.add(toPut(model)));
        doSaveOperation(putList);
        return entities;
    }

    @Override
    public Optional<T> findById(ID id) {
        return null;
    }

    @Override
    public boolean existsById(ID id) {
        return false;
    }

    @Override
    public Iterable<T> findAll() {
        return null;
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return null;
    }

    @Override
    public long count() {
        Table table = getConnectionTable();
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
        finally {
            closeTable(table);
        }
        return rowCount;
    }

    @Override
    public void deleteById(ID id) {

    }

    @Override
    public void delete(T entity) {
        Delete delete = new Delete(Bytes.toBytes(getRowkey(entity)));

        doDeleteOperation(Arrays.asList(delete));
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        List<Delete> deletes = new ArrayList<>();
        entities.forEach(model -> deletes.add(new Delete(Bytes.toBytes(getRowkey(model)))));

        doDeleteOperation(deletes);
    }

    @Override
    public void deleteAll() {

    }

    private void doSaveOperation(List<Put> puts) {
        Table table = getConnectionTable();
        try {
            table.put(puts);
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        finally {
            closeTable(table);
        }
    }

    private void doDeleteOperation(List<Delete> deletes) {
        Table table = getConnectionTable();
        try {
            table.delete(deletes);
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        finally {
            closeTable(table);
        }
    }

    private void closeTable(Table table){
        try {
            table.close();
        }
        catch (IOException e) {
            //关闭table异常说明连接异常，连接不能回收
            throw new ConnectionException(e.getMessage());
        }
        finally {
            //关闭table出现异常，连接也要关闭
            try {
                CURRENT_CONNECTION.get().close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Table getConnectionTable() {
        Connection connection = connectionProvider.getConnection();
        Table table;
        try {
            table = connection.getTable(TableName.valueOf(hbaseEntity.getTableName()));
        }
        catch (IOException e) {
            throw new ConnectionException("获取不到表: " + hbaseEntity.getTableName()
                                          + " "
                                          + e.getMessage());
        }
        finally {
            connectionProvider.recycleConnection(connection);
        }
        //有异常就不会执行到这里了,没异常才会到这里
        CURRENT_CONNECTION.set(connection);
        return table;
    }

    private byte[] convertToByte(Object object) {
        if (object instanceof Float) {
            return Bytes.toBytes((Float) object);
        }
        if (object instanceof Double) {
            return Bytes.toBytes((Double) object);
        }
        if (object instanceof BigDecimal) {
            return Bytes.toBytes((BigDecimal) object);
        }
        if (object instanceof Long) {
            return Bytes.toBytes((Long) object);
        }
        if (object instanceof Date) {
            return Bytes.toBytes(((Date) object).getTime());
        }
        if (object instanceof String) {
            return Bytes.toBytes((String) object);
        }
        return Bytes.toBytes(object.toString());
    }

}
