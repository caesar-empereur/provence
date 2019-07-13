package com.app.repository.mongodb;

import com.app.model.mongodb.MongoAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/2/1.
 */
@Repository
public interface MongoAccountRepository extends MongoRepository<MongoAccount, String> {
}
