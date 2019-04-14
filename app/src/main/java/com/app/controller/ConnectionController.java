package com.app.controller;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import com.app.model.hbase.OrderRecord;
import com.app.model.jpa.Order;
import com.app.pojo.OrderPojo;
import com.app.repository.hbase.OrderRecordRepository;
import com.app.repository.jpa.OrderRepository;
import com.app.repository.mongodb.MongoAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.model.mongodb.MongoAccount;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/9/17.
 */
@Api(description = "guest接口")
@RestController
@RequestMapping("/connection")
public class ConnectionController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${hbase.zookeeper.quorum}")
    private String quorum;
    
    @Value("${hbase.zookeeper.port}")
    private String port;
    
    @Value("${table.name}")
    private String tableName;
    
    @Value("${hadoop.dir}")
    private String hadoopDir;

    @Resource
    private MongoAccountRepository mongoAccountRepository;

    @Resource
    private OrderRecordRepository orderRecordRepository;

    @Resource
    private OrderRepository orderRepository;
    
    @ApiOperation(value = "count")
    @GetMapping(value = "/mongo/save")
    public void save(){
        MongoAccount mongoAccount = new MongoAccount();
        mongoAccount.setBalance(10.00);
        mongoAccount.setCreateTime(new Date());
        mongoAccount.setUsername(UUID.randomUUID().toString());
        mongoAccountRepository.save(mongoAccount);
    }

    @ApiOperation(value = "hbase保存")
    @GetMapping(value = "/hbase/save")
    public void hbaseSave(){
        OrderPojo orderPojo = new OrderPojo();

        orderPojo.setProductId(UUID.randomUUID().toString());
        orderPojo.setProductName("product-name");
        orderPojo.setProductPrice(10.20);
        orderPojo.setProductType("phone");

        orderPojo.setPaymentId(UUID.randomUUID().toString());
        orderPojo.setPaymentAmount(10.20);
        orderPojo.setPaymentDiscount(1.20);
        orderPojo.setPaymentType("alipay");

        Order order = new Order();
        BeanUtils.copyProperties(orderPojo, order);
        order.setCreateTime(new Date());
        orderRepository.save(order);

        OrderRecord orderRecord = new OrderRecord();
        BeanUtils.copyProperties(orderPojo, orderRecord);
        orderRecord.setOrderId(order.getId());
        orderRecord.setOrderDate(new Date().getTime());

        orderRecordRepository.save(orderRecord);

    }

}
