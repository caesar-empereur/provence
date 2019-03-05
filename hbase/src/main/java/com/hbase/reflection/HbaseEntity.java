package com.hbase.reflection;

import com.hbase.core.FamilyColumn;
import org.springframework.data.repository.core.EntityInformation;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
public interface HbaseEntity<T, ID> extends EntityInformation<T, ID> {

    String getTableName();

    Map<String, Class> getRowkeyColumns();

    List<FamilyColumn> getFamilyColumnList();
}
