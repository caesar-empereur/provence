package com.app.model;

import com.app.annotation.HTableColum;
import com.app.annotation.HTableColumFamily;
import com.app.annotation.HbaseTable;
import com.app.annotation.RowKey;

/**
 * Created by yang on 2018/6/10.
 */

@HbaseTable(name = "user")
public class User {
    
    @RowKey
    private String id;

    @HTableColumFamily(name = "base-info")
    @HTableColum
    private String username;
    
    @HTableColumFamily(name = "base-info")
    @HTableColum
    private String password;
    
    @HTableColumFamily(name = "contact-info")
    @HTableColum
    private String phone;
    
    @HTableColumFamily(name = "contact-info")
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
