package com.app.model.jpa;

import com.app.pojo.OrderPojo;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/26.
 */
@Entity
@Table(name = "orders")
public class Order extends OrderPojo {

    private String id;

    private Date createTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
