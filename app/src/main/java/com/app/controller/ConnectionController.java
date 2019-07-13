package com.app.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import com.app.model.hbase.OrderRecord;
import com.app.model.jpa.Order;
import com.app.model.jpa.OrderHistory;
import com.app.pojo.OrderPojo;
import com.app.repository.hbase.OrderRecordRepository;
import com.app.repository.jpa.OrderHistoryRepository;
import com.app.repository.jpa.OrderRepository;
import com.app.repository.mongodb.MongoAccountRepository;
import com.app.util.UUIDUtil;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

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

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

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
    private OrderHistoryRepository orderHistoryRepository;

    @Resource
    private OrderRepository orderRepository;
    
    @ApiOperation(value = "mongo操作")
    @GetMapping(value = "/mongo/save")
    public void save(){
        MongoAccount mongoAccount = new MongoAccount();
        mongoAccount.setBalance(10.00);
        mongoAccount.setCreateTime(new Date());
        mongoAccount.setUsername(UUIDUtil.randomUUID());
        mongoAccountRepository.save(mongoAccount);
    }

    @ApiOperation(value = "hbase保存")
    @GetMapping(value = "/hbase/save")
    public void hbaseSave() {
        
        List<OrderRecord> orderRecordList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            OrderPojo orderPojo = new OrderPojo();
            
            orderPojo.setProductId(UUIDUtil.randomUUID());
            orderPojo.setProductName("product-name");
            orderPojo.setProductPrice(10.20);
            orderPojo.setProductType("phone");
            
            orderPojo.setPaymentId(UUIDUtil.randomUUID());
            orderPojo.setPaymentAmount(10.20);
            orderPojo.setPaymentDiscount(1.20);
            orderPojo.setPaymentType("alipay");
            
            Date date = new Date();
            
            OrderRecord orderRecord = new OrderRecord();
            BeanUtils.copyProperties(orderPojo, orderRecord);
            orderRecord.setOrderId(UUIDUtil.randomUUID());
            orderRecord.setOrderDate(date.getTime());
            
            orderRecordList.add(orderRecord);
        }
        long start = System.currentTimeMillis();
        orderRecordRepository.saveAll(orderRecordList);
        log.info("消耗时间：" + (System.currentTimeMillis() - start) / 1000);
    }

//    @ApiOperation(value = "hbase 查询")
//    @PostMapping(value = "/hbase/scan")
//    public List<OrderRecord> scan(@ApiParam @RequestBody ScanQuest scanQuest){
//        return new ArrayList<>(orderRecordRepository.scan(scanQuest.getStart(), scanQuest.getEnd()));
//    }

//    @ApiOperation(value = "hbase get")
//    @PostMapping(value = "/hbase/get/{rk}")
//    public OrderRecord get(@PathVariable String rk){
//        return orderRecordRepository.findByRowkey(rk);
//    }

    @Data
    static class ScanQuest{
        private String start;
        private String end;
    }
}
