package com.hbase.repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.util.*;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
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
import org.springframework.util.CollectionUtils;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
public class SimpleHbaseRepository<T, RK> implements HbaseRepository<T, RK> {

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();

    private ConnectionProvider<Connection> connectionProvider = ConnectionPoolManager.getInstance();

    private HbaseEntity<T, RK> hbaseEntity;

    private Map<String, FamilyColumn> familyColumnMap;

    public SimpleHbaseRepository(HbaseEntity<T, RK> hbaseEntity) {
        this.hbaseEntity = Optional.of(hbaseEntity).get();
        familyColumnMap = new HashMap<>();
        for (FamilyColumn familyColumn : hbaseEntity.getFamilyColumnList()){
            familyColumnMap.put(familyColumn.getColumnName(), familyColumn);
        }
    }

    @Override
    public T save(T entity) {
        doSaveOperation(Arrays.asList(toPut(entity)));
        return entity;
    }

    private Put toPut(T entity){
        Put put = new Put(convertToByte(hbaseEntity.getRowkey(entity)));
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        for (Map.Entry<String, Object> objectKeyValue : entityMap.entrySet()){
            if (objectKeyValue.getValue() == null
                || hbaseEntity.getRowkeyColumnMap().get(objectKeyValue.getKey()) != null) {
                entityMap.remove(objectKeyValue.getKey());
                continue;
            }
            String familyName = familyColumnMap.get(objectKeyValue.getKey()).getFamilyName();
            String columnName = objectKeyValue.getKey();
            put.addColumn(familyName.getBytes(),
                          columnName.getBytes(),
                          convertToByte(objectKeyValue.getValue()));
        }
        return put;
    }

    @Override
    public Collection<T> saveAll(Collection<T> entities) {
        List<Put> putList = new ArrayList<>();
        entities.forEach(model -> putList.add(toPut(model)));
        doSaveOperation(putList);
        return entities;
    }

    @Override
    public T findByRowkey(RK id) {
        Get get = new Get(convertToByte(id));
        List<T> entitys = doGetOperation(Arrays.asList(get));
        if (CollectionUtils.isEmpty(entitys)){
            return null;
        }
        return entitys.get(0);
    }

    @Override
    public boolean existsByRowkey(RK id) {
        Get get = new Get(convertToByte(id));
        List<T> entitys = doGetOperation(Arrays.asList(get));
        return CollectionUtils.isEmpty(entitys);
    }

    @Override
    public Collection<T> findAllByRowkey(Collection<RK> rks) {
        List<Get> gets = new ArrayList<>();
        rks.forEach(rk -> gets.add(new Get(convertToByte(rk))));
        return doGetOperation(gets);
    }

    @Override
    public Collection<T> scan(RK start, RK end) {
        Scan scan = new Scan().withStartRow(convertToByte(start)).withStopRow(convertToByte(end));
        Table table = getConnectionTable();
        ResultScanner resultScanner;
        try {
            resultScanner = table.getScanner(scan);
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        return resolveResult(resultScanner);
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
    public void deleteByRowkey(RK rk) {
        Delete delete = new Delete(convertToByte(rk));

        doDeleteOperation(Arrays.asList(delete));
    }

    @Override
    public void deleteAll(Collection<RK> rks) {
        List<Delete> deletes = new ArrayList<>();
        rks.forEach(rk -> deletes.add(new Delete(convertToByte(rk))));

        doDeleteOperation(deletes);
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

    private List<T> doGetOperation(List<Get> puts) {
        Table table = getConnectionTable();

        try {
            Result[] results = table.get(puts);
            return resolveResult(Arrays.asList(results));
        }
        catch (IOException e) {
            throw new OperationException(e.getMessage());
        }
        finally {
            closeTable(table);
        }
    }

    private List<T> resolveResult(Iterable<Result> results){
        List<T> entityList = new ArrayList<>();
        if (results == null){
            return entityList;
        }
        for (Result result : results){
            Map<String,Object> entityMap = new HashMap<>();
            for (Cell cell : result.listCells()){
                entityMap.put(Bytes.toString(CellUtil.cloneQualifier(cell)),
                              byteToObject(CellUtil.cloneValue(cell)));
            }
            T entity = JSON.parseObject(JSON.toJSONString(entityMap), hbaseEntity.getJavaType());
            entityList.add(entity);
        }
        return entityList;
    }

    private Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
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
