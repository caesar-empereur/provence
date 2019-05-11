package com.hbase.repository;

import com.alibaba.fastjson.JSON;
import com.hbase.reflection.HbaseEntity;
import com.hbase.reflection.RowkeyInfo;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/4/27.
 */
public class RowkeyGenerator<T, RK> {
    
    private HbaseEntity hbaseEntity;
    
    private Class finalRKClass;
    
    public RowkeyGenerator(HbaseEntity hbaseEntity, Class finalRKClass) {
        this.hbaseEntity = hbaseEntity;
        this.finalRKClass = finalRKClass;
    }
    
    public RK getRowkey(T entity) {
        Map<String, Object> entityMap = JSON.parseObject(JSON.toJSONString(entity), Map.class);
        // 字段为空的需要 去除掉
        Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> objectKeyValue = iterator.next();
            if (objectKeyValue.getValue() == null) {
                iterator.remove();
            }
        }
        Collections.sort(hbaseEntity.getRowkeyInfoList());
        if (finalRKClass == Long.class) {
            Long rowkey = 0L;
            // 生成 rowkey
            for (Object rowkeyInfo : hbaseEntity.getRowkeyInfoList()) {
                rowkey = rowkey
                         + entityMap.get(((RowkeyInfo) rowkeyInfo).getField().getName()).hashCode();
            }
            return (RK) rowkey;
        }
        StringBuilder rowkey = new StringBuilder("");
        int i = 0;
        for (Object rowkeyInfo : hbaseEntity.getRowkeyInfoList()) {
            if (i > 0 && i < hbaseEntity.getRowkeyInfoList().size()) {
                rowkey.append("-");
            }
            rowkey.append(entityMap.get(((RowkeyInfo) rowkeyInfo).getField().getName()));
            i++;
        }
        return (RK) rowkey;
    }
}
