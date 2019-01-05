package com.app.model.hbase;

import java.util.Date;

import com.hbase.annotation.*;

/**
 * Created by leon on 2018/4/11.
 */
@HbaseTable(name = "account")
@CompoundColumFamily(columnFamily = { @ColumnFamily(name = "base-info", unique = true),
                                      @ColumnFamily(name = "balance-info", unique = true) }, constraint = true)
@RowKey(columnList = { "id" })
public class Account {
    
    private String username;
    
    private Date createAt;
    
    private String id;
    
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
