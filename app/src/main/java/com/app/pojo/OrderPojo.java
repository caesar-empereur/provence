package com.app.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/26.
 */
public class OrderPojo {


    private String productId;

    private String productName;

    private Double productPrice;

    private String productType;


    private String paymentId;

    private Double paymentAmount;

    private Double paymentDiscount;

    private String paymentType;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(Double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Double getPaymentDiscount() {
        return paymentDiscount;
    }

    public void setPaymentDiscount(Double paymentDiscount) {
        this.paymentDiscount = paymentDiscount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
}
