package com.app.model.hbase;

import com.app.pojo.OrderPojo;
import com.hbase.annotation.ColumnFamily;
import com.hbase.annotation.CompoundColumFamily;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;
import lombok.Data;

/**
 * Created by yang on 2019/3/10.
 */
@HbaseTable(name = "order-record")
@CompoundColumFamily(columnFamily = { @ColumnFamily(name = "product", columnList = { "productId", "productName", "productPrice", "productType" }, unique = true),
                                      @ColumnFamily(name = "payment", columnList = { "paymentId", "paymentAmount", "paymentDiscount", "paymentType" }, unique = true) }, constraint = true)
@RowKey(columnList = { "orderId", "orderDate" })
@Data
public class OrderRecord extends OrderPojo{

    private String orderId;

    private Long orderDate;

}
