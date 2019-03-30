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
@RowKey(columnList = { "orderId", "orderDate" })
@Data
public class OrderRecord extends OrderPojo {
    
    private String orderId;
    
    private Long orderDate;

    @ColumnFamily(name = "")
    @Override
    public String getProductId() {
        return super.getProductId();
    }
    
    @Override
    public String getProductName() {
        return super.getProductName();
    }
    
    @Override
    public Double getProductPrice() {
        return super.getProductPrice();
    }
    
    @Override
    public String getProductType() {
        return super.getProductType();
    }
    
    @Override
    public String getPaymentId() {
        return super.getPaymentId();
    }
    
    @Override
    public Double getPaymentAmount() {
        return super.getPaymentAmount();
    }
    
    @Override
    public Double getPaymentDiscount() {
        return super.getPaymentDiscount();
    }
    
    @Override
    public String getPaymentType() {
        return super.getPaymentType();
    }
}
