package com.hbase.repository;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.hbase.reflection.RowkeyInfo;
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

    private Map<String, RowkeyInfo> rowkeyInfoMap;

    public SimpleHbaseRepository(HbaseEntity<T, RK> hbaseEntity) {
        this.hbaseEntity = Optional.of(hbaseEntity).get();
        this.familyColumnMap = new HashMap<>();
        this.rowkeyInfoMap = new HashMap<>();
        for (FamilyColumn familyColumn : hbaseEntity.getFamilyColumnList()){
            this.familyColumnMap.put(familyColumn.getColumnName(), familyColumn);
        }
        for (RowkeyInfo rowkeyInfo : hbaseEntity.getRowkeyInfoList()){
            this.rowkeyInfoMap.put(rowkeyInfo.getField().getName(), rowkeyInfo);
        }
    }

    @Override
    public T save(T entity) {
        doSaveOperation(Arrays.asList(toPut(entity)));
        return entity;
    }

    private Put toPut(T entity){
        Put put = new Put(convertRowkeyToByte(hbaseEntity.getRowkey(entity)));
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        //过滤掉值为空的，和 rowkey 的字段
        Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, Object> objectKeyValue = iterator.next();
            if (objectKeyValue.getValue() == null
//                || this.rowkeyInfoMap.get(objectKeyValue.getKey()) != null
                    ) {
                iterator.remove();
                continue;
            }
            String familyName = familyColumnMap.get(objectKeyValue.getKey()).getFamilyName();
            String columnName = objectKeyValue.getKey();
            put.addColumn(familyName.getBytes(),
                          columnName.getBytes(),
//                          convertToByte(objectKeyValue.getValue().toString()));
                          Bytes.toBytes(objectKeyValue.getValue().toString()));

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
        Get get = new Get(convertRowkeyToByte(id));
        List<T> entitys = doGetOperation(Arrays.asList(get));
        if (CollectionUtils.isEmpty(entitys)){
            return null;
        }
        return entitys.get(0);
    }

    @Override
    public boolean existsByRowkey(RK id) {
        Get get = new Get(convertRowkeyToByte(id));
        List<T> entitys = doGetOperation(Arrays.asList(get));
        return CollectionUtils.isEmpty(entitys);
    }

    @Override
    public Collection<T> findAllByRowkey(Collection<RK> rks) {
        List<Get> gets = new ArrayList<>();
        rks.forEach(rk -> gets.add(new Get(convertRowkeyToByte(rk))));
        return doGetOperation(gets);
    }

    @Override
    public Collection<T> scan(RK start, RK end) {
        Scan scan = new Scan().withStartRow(convertRowkeyToByte(start))
                              .withStopRow(convertRowkeyToByte(end));
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
        Delete delete = new Delete(convertRowkeyToByte(rk));

        doDeleteOperation(Arrays.asList(delete));
    }

    @Override
    public void deleteAll(Collection<RK> rks) {
        List<Delete> deletes = new ArrayList<>();
        rks.forEach(rk -> deletes.add(new Delete(convertRowkeyToByte(rk))));

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
                              convertToObject(cell));

                String rowkeyValue = Bytes.toString(CellUtil.cloneRow(cell));
                if (rowkeyValue.contains("-")){

                }
            }
            T entity = JSON.parseObject(JSON.toJSONString(entityMap), hbaseEntity.getJavaType());
            entityList.add(entity);
        }
        return entityList;
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
                CURRENT_CONNECTION.remove();
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

    private byte[] convertRowkeyToByte(Object object) {
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

    private Object convertToObject(Cell cell){
        String objectKeyName = Bytes.toString(CellUtil.cloneQualifier(cell));
        if (!familyColumnMap.containsKey(objectKeyName)){
            return null;
        }
        String value = Bytes.toString(CellUtil.cloneValue(cell));
        Class clazz = familyColumnMap.get(objectKeyName).getColumnType();
        if (clazz == Float.class){
            return Float.valueOf(value);
        }
        if (clazz == Double.class){
            return Double.valueOf(value);
        }
        if (clazz == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (clazz == Long.class){
            return Long.valueOf(value);
        }
        if (clazz == Date.class){
            return new Date(Long.valueOf(value));
        }
        if (clazz == String.class){
            return value;
        }
        return value;
    }

}
