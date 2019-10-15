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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Resource
    private OrderRecordHbaseRepository orderRecordRepository;

    @Resource
    private OrderHistoryRepository orderHistoryRepository;

    @Resource
    private OrderRepository orderRepository;
    
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

    @Data
    static class ScanQuest{
        private String start;
        private String end;
    }
}
