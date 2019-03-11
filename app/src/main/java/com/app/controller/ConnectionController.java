package com.app.controller;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import com.app.model.hbase.OrderRecord;
import com.app.repository.hbase.OrderRecordRepository;
import com.app.repository.mongodb.MongoAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    @ApiOperation(value = "count")
    @GetMapping(value = "/mongo/save")
    public void save(){
        MongoAccount mongoAccount = new MongoAccount();
        mongoAccount.setBalance(10.00);
        mongoAccount.setCreateTime(new Date());
        mongoAccount.setUsername(UUID.randomUUID().toString());
        mongoAccountRepository.save(mongoAccount);
    }

    @ApiOperation(value = "count")
    @GetMapping(value = "/mongo/find")
    public void select(){
//        MongoAccount mongoAccount = mongoAccountRepository.findById("5c53bfa846e356252cdf8400").get();
    }

    @ApiOperation(value = "hbase保存")
    @GetMapping(value = "/hbase/save")
    public void hbaseSave(){
        OrderRecord orderRecord = new OrderRecord();
        orderRecord.setOrderId(UUID.randomUUID().toString());
        orderRecord.setOrderDate((new Date()).getTime());

        orderRecord.setProductId(UUID.randomUUID().toString());
        orderRecord.setProductName("product-name");
        orderRecord.setProductPrice(10.20);
        orderRecord.setProductType("phone");

        orderRecord.setPaymentId(UUID.randomUUID().toString());
        orderRecord.setPaymentAmount(10.20);
        orderRecord.setPaymentDiscount(1.20);
        orderRecord.setPaymentType("alipay");

        orderRecordRepository.save(orderRecord);

    }

}
