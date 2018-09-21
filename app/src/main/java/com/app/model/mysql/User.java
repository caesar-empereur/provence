package com.app.model.mysql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;

import com.app.pojo.mysql.UserPojo;

/**
 * Created by yang on 2018/6/10.
 */

@Entity
public class User extends UserPojo {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    @Override
    public String getId() {
        return super.getId();
    }
    
    @Override
    public String getPassword() {
        return super.getPassword();
    }
    
    @Override
    public String getEmail() {
        return super.getEmail();
    }
    
    @Override
    public String getUsername() {
        return super.getUsername();
    }
    
    @Override
    public String getPhone() {
        return super.getPhone();
    }
}
