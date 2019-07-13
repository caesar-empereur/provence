package com.app.model.mongodb;

import com.app.pojo.AccountPojo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
@Document(collection = "account")
public class MongoAccount extends AccountPojo {

    @Id
    @Override
    public String getId() {
        return super.getId();
    }
}

