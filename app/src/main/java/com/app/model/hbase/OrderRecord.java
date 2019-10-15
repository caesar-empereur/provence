package com.app.model.hbase;

import com.app.pojo.OrderPojo;
import com.hbase.annotation.ColumnFamily;
import com.hbase.annotation.HbaseTable;
import com.hbase.annotation.RowKey;

import lombok.Data;

/**
 * Created by yang on 2019/3/10.
 */
@HbaseTable(name = "order-record")
@Data
public class OrderRecord extends OrderPojo {

    @RowKey(order = 1)
    @ColumnFamily(name = "payment")
    private String orderId;

    @ColumnFamily(name = "payment")
    private Long orderDate;

    @ColumnFamily(name = "product")
    @Override
    public String getProductId() {
        return super.getProductId();
    }

    @ColumnFamily(name = "product")
    @Override
    public String getProductName() {
        return super.getProductName();
    }

    @ColumnFamily(name = "product")
    @Override
    public Double getProductPrice() {
        return super.getProductPrice();
    }

    @ColumnFamily(name = "payment")
    @Override
    public String getPaymentId() {
        return super.getPaymentId();
    }

    @ColumnFamily(name = "payment")
    @Override
    public Double getPaymentAmount() {
        return super.getPaymentAmount();
    }

    @ColumnFamily(name = "payment")
    @Override
    public String getPaymentType() {
        return super.getPaymentType();
    }
}
