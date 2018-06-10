package com.app.model;

import com.app.annotation.HTableColum;
import com.app.annotation.HbaseTable;
import com.app.annotation.RowKey;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by leon on 2018/4/11.
 */
@HbaseTable(name = "account")
@Entity
public class Account {

    @HTableColum
    private String username;

    @HTableColum
    private Date createAt;

    @RowKey
    private String id;

    @HTableColum
    private Double balance;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
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
