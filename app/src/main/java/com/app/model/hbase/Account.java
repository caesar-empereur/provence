package com.app.model.hbase;

import java.util.Date;

import com.app.pojo.StringId;
import com.hbase.annotation.*;
import lombok.Data;

/**
 * Created by leon on 2018/4/11.
 */
@HbaseTable(name = "account")
@CompoundColumFamily(columnFamily = { @ColumnFamily(name = "base-info", unique = true),
                                      @ColumnFamily(name = "balance-info", unique = true) }, constraint = true)
@RowKey(columnList = { "id" })
@Data
public class Account extends StringId {
    
    private String username;
    
    private Date createAt;
    
    private Double balance;
    
}
