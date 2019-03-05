package com.hbase.repository;

import java.io.IOException;
import java.util.*;

import com.hbase.core.OperationType;
import com.hbase.reflection.HbaseEntity;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FirstKeyOnlyFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.alibaba.fastjson.JSON;
import com.hbase.exception.ConnectionException;
import com.hbase.exception.OperationException;
import com.hbase.pool.ConnectionProvider;
import com.hbase.pool.hibernate.ConnectionPoolManager;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class SimpleHbaseRepository<T, ID> implements HbaseRepository<T, ID> {

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    private ConnectionProvider<Connection> connectionProvider = ConnectionPoolManager.getInstance();

    private HbaseEntity<T, ID> entityInformation;

    public SimpleHbaseRepository(HbaseEntity<T, ID> entityInformation) {
        this.entityInformation = Optional.of(entityInformation).get();
    }

    private int getRowkey(T entity){
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        //字段为空的需要 去除掉
        for (Map.Entry<String, Object> entry : entityMap.entrySet()){
            if (entry.getValue() == null){
                entityMap.remove(entry.getKey());
            }
        }
        int rowkey = 0;
        // 生成 rowkey
        Set<Map.Entry<String, Class>> entrySet = entityInformation.getRowkeyColumns().entrySet();
        for (Map.Entry<String, Class> entry : entrySet) {
            rowkey = rowkey + entityMap.get(entry.getKey()).hashCode();
        }
        return rowkey;
    }

    @Override
    public <S extends T> S save(S entity) {
        Table table = getConnectionTable();
        Put put = new Put(Bytes.toBytes(getRowkey(entity)));
        put.addColumn("aaa".getBytes(), "balance".getBytes(), Bytes.toBytes(10.00));
        put.addColumn("bbb".getBytes(), "username".getBytes(), Bytes.toBytes("heheda"));

        doOperation(table, put, OperationType.SAVE);
        return entity;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Table table = getConnectionTable();
        List<Put> putList = new ArrayList<>();
        entities.forEach(model -> putList.add(new Put(Bytes.toBytes(getRowkey(model)))));
        doOperation(table, putList, OperationType.SAVE);
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
        Table table = getConnectionTable();;
        Delete delete = new Delete(Bytes.toBytes(getRowkey(entity)));

        doOperation(table, delete, OperationType.DELETE);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Table table = getConnectionTable();
        List<Delete> deletes = new ArrayList<>();
        entities.forEach(model -> deletes.add(new Delete(Bytes.toBytes(getRowkey(model)))));

        doOperation(table, deletes, OperationType.DELETE);
    }

    @Override
    public void deleteAll() {

    }

    private void doOperation(Table table,
                             Object parameter,
                             OperationType operationType) {
        try {
            if (operationType == OperationType.SAVE) {
                table.put((Put) parameter);
                return;
            }
            if (operationType == OperationType.DELETE) {
                table.delete((Delete) parameter);
            }
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
            table = connection.getTable(TableName.valueOf(entityInformation.getTableName()));
        }
        catch (IOException e) {
            throw new ConnectionException("获取不到表: " + entityInformation.getTableName()
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
}
