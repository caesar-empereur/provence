package com.app.model;

import com.hbase.annotation.HTableColum;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;

/**
 * Created by yang on 2018/6/10.
 */

@HbaseTable(name = "user")
public class User {
    
    @RowKey
    private String id;

    @HTableColum
    private String username;
    
    @HTableColum
    private String password;
    
    @HTableColum
    private String phone;
    
    @HTableColum
    private String email;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
