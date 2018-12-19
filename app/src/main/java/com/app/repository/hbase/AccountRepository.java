package com.app.repository.hbase;

import com.app.model.hbase.Account;
import com.hbase.annotation.HbaseRepository;
import com.hbase.repository.HbaseCrudRepository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
@HbaseRepository
public interface AccountRepository extends HbaseCrudRepository<String, Account> {
}
