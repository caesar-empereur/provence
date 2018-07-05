package com.app.model;

import java.util.Date;

import com.hbase.annotation.*;

/**
 * Created by leon on 2018/4/11.
 */
@HbaseTable(name = "account")
@CompoundColumFamily(columnFamily = { @ColumnFamily(name = "base-info", columnList = { "username", "id" }, unique = true),
                                      @ColumnFamily(name = "balance-info", columnList = { "balance" }, unique = true) }, constraint = true)
public class Account {
    
    @HTableColum
    private String username;
    
    @HTableColum
    private Date createAt;
    
    @RowKey
    private String id;
    
    @HTableColum
    private Double balance;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Date getCreateAt() {
        return createAt;
    }
    
    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
