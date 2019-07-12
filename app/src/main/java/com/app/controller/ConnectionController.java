package com.app.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import com.app.model.hbase.OrderRecord;
import com.app.model.jpa.Order;
import com.app.model.jpa.OrderHistory;
import com.app.pojo.OrderPojo;
import com.app.repository.hbase.OrderRecordHbaseRepository;
import com.app.repository.jpa.OrderHistoryRepository;
import com.app.repository.jpa.OrderRepository;
import com.app.util.UUIDUtil;
import io.swagger.annotations.ApiParam;
import lombok.Data;
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

    private int num = 63000;

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

//    @Resource
//    private MongoAccountRepository mongoAccountRepository;

    @Resource
    private OrderRecordHbaseRepository orderRecordRepository;

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
//        mongoAccountRepository.save(mongoAccount);
    }

    @ApiOperation(value = "hbase保存")
    @GetMapping(value = "/hbase/save")
    public void hbaseSave() {
        for (int i= 0; i<5000; i++){
            OrderPojo orderPojo = new OrderPojo();
            orderPojo.setProductId(UUIDUtil.randomUUID());
            orderPojo.setProductName("product-name");
            orderPojo.setProductPrice(10.20);
            orderPojo.setPaymentId(UUIDUtil.randomUUID());
            orderPojo.setPaymentAmount(10.20);
            orderPojo.setPaymentType("alipay");

            Order order = new Order();
            BeanUtils.copyProperties(orderPojo, order);
            order.setId(UUIDUtil.randomUUID());
            order.setCreateTime(new Date());
            orderRepository.save(order);

            List<OrderRecord> orderRecordList = new ArrayList<>();
            List<OrderHistory> orderHistoryList = new ArrayList<>();
            for (int j = 0; j < 300; j++) {

                Date date = getDate(num++);

                OrderRecord orderRecord = new OrderRecord();
                BeanUtils.copyProperties(orderPojo, orderRecord);
                orderRecord.setOrderId(order.getId());
                orderRecord.setOrderDate(date.getTime());

                OrderHistory orderHistory = new OrderHistory();
                BeanUtils.copyProperties(orderRecord, orderHistory);

                orderHistory.setOrderDate(date);
                orderHistoryList.add(orderHistory);
                orderRecordList.add(orderRecord);
            }
            long start = System.currentTimeMillis();
            orderRecordRepository.saveAll(orderRecordList);
            orderHistoryRepository.saveAll(orderHistoryList);
            log.info("消耗时间：" + (System.currentTimeMillis() - start) / 1000);
        }
    }

    private  Date getDate(int num) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(new Date());
        rightNow.add(Calendar.HOUR, -num);
        return rightNow.getTime();
    }

    @ApiOperation(value = "hbase scan")
    @PostMapping(value = "/hbase/scan")
    public List<OrderRecord> scan(@ApiParam @RequestBody ScanQuest scanQuest){
        return new ArrayList<>(orderRecordRepository.scan(scanQuest.getStart(), scanQuest.getEnd()));
    }

    @ApiOperation(value = "hbase get")
    @PostMapping(value = "/hbase/get/{rk}")
    public OrderRecord get(@PathVariable String rk){
        return orderRecordRepository.findByRowkey(rk);
    }

    @Data
    static class ScanQuest{
        private String start;
        private String end;
    }
}
