package com.app;

import com.hbase.repository.HbaseRepository;

/**
 * Created by yang on 2019/3/10.
 */
@com.hbase.annotation.HbaseRepository
public interface OrderRecordHbaseRepository extends HbaseRepository<OrderRecord, String>{
}
