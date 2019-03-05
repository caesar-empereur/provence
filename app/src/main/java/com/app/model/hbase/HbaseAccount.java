package com.app.model.hbase;

import com.app.pojo.AccountPojo;
import com.hbase.annotation.ColumnFamily;
import com.hbase.annotation.CompoundColumFamily;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;

/**
 * Created by leon on 2018/4/11.
 */
@HbaseTable(name = "account", id = "id")
@CompoundColumFamily(columnFamily = { @ColumnFamily(name = "base-info", columnList = { "username", "createTime" }, unique = true),
                                      @ColumnFamily(name = "balance-info", columnList = { "balance" }, unique = true) }, constraint = true)
@RowKey(columnList = { "id" })
public class HbaseAccount extends AccountPojo {
    
}
