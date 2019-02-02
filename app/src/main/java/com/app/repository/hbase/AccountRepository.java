package com.app.repository.hbase;

import com.app.model.hbase.HbaseAccount;
import com.hbase.repository.HbaseRepository;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/12/17.
 */
@com.hbase.annotation.HbaseRepository
public interface AccountRepository extends HbaseRepository<String, HbaseAccount> {
}
