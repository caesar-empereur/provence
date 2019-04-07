package com.app.repository.hbase;

import com.app.model.hbase.OrderRecord;
import com.hbase.repository.HbaseRepository;

/**
 * Created by yang on 2019/3/10.
 */
//@com.hbase.annotation.HbaseRepository
public interface OrderRecordRepository extends HbaseRepository<OrderRecord, String>{
}
