package com.app;

import com.hbase.annotation.ColumnFamily;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;

import lombok.Data;

/**
 * Created by yang on 2019/3/10.
 */
@HbaseTable(name = "order-record")
@Data
public class OrderRecord {

    @ColumnFamily(name = "product")
    private String productId;

    @ColumnFamily(name = "product")
    private String productName;

    @ColumnFamily(name = "product")
    private Double productPrice;

    @ColumnFamily(name = "payment")
    private String paymentId;

    @ColumnFamily(name = "payment")
    private Double paymentAmount;

    @ColumnFamily(name = "payment")
    private String paymentType;

    @RowKey(order = 1)
    @ColumnFamily(name = "payment")
    private String orderId;

    @ColumnFamily(name = "payment")
    private Long orderDate;

}
