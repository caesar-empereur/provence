package com.app.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2019/3/26.
 */
@Data
public class OrderPojo {


    private String productId;

    private String productName;

    private Double productPrice;

    private String productType;


    private String paymentId;

    private Double paymentAmount;

    private Double paymentDiscount;

    private String paymentType;

}