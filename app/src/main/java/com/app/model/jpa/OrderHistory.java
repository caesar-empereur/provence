package com.app.model.jpa;

import com.app.pojo.OrderPojo;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/4/27.
 */
@Entity
@Table(name = "order_history")
public class OrderHistory extends OrderPojo {
    
    private String id;
    
    private String orderId;
    
    private Date orderDate;

    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @Column(length = 36)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

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
    public String getPaymentId() {
        return super.getPaymentId();
    }
    
    @Override
    public Double getPaymentAmount() {
        return super.getPaymentAmount();
    }
    
    @Override
    public String getPaymentType() {
        return super.getPaymentType();
    }
}
