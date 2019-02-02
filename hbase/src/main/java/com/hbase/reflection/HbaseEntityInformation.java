package com.hbase.reflection;

import org.springframework.data.repository.core.EntityInformation;

import java.util.Map;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
public interface HbaseEntityInformation<T, ID> extends EntityInformation<T, ID> {

    String getTableName();

    Map<String, Class> getRowkeyColumns();
}
