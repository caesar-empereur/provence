package com.app.model.hbase;

import com.hbase.annotation.ColumnFamily;
import com.hbase.annotation.CompoundColumFamily;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;
import lombok.Data;

/**
 * Created by yang on 2019/3/10.
 */
@HbaseTable(name = "account")
@CompoundColumFamily(columnFamily = { @ColumnFamily(name = "product", columnList = { "productId", "productName", "productPrice", "productType" }, unique = true),
                                      @ColumnFamily(name = "payment", columnList = { "paymentId", "paymentAmount", "paymentDiscount", "paymentType" }, unique = true) }, constraint = true)
@RowKey(columnList = { "orderId", "orderDate" })
@Data
public class OrderRecord {

    private String orderId;

    private Long orderDate;


    private String productId;

    private String productName;

    private Double productPrice;

    private String productType;


    private String paymentId;

    private Double paymentAmount;

    private Double paymentDiscount;

    private String paymentType;
}
