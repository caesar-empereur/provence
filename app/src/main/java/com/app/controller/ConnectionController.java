package com.app.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.app.model.mongodb.MongoAccount;
import com.app.repository.mongodb.MongoAccountRepository;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.hibernate.cfg.AvailableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaxxer.hikari.hibernate.HikariConnectionProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.annotation.Resource;

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

//    @Resource
//    private AccountRepository accountRepository;
    
    @ApiOperation(value = "count")
    @GetMapping(value = "/mongo/save")
    public void save(){
        MongoAccount mongoAccount = new MongoAccount();
        mongoAccount.setBalance(10.00);
        mongoAccount.setCreateAt(new Date());
        mongoAccount.setUsername(UUID.randomUUID().toString());
        mongoAccountRepository.save(mongoAccount);
    }

    @ApiOperation(value = "count")
    @GetMapping(value = "/mongo/find")
    public void select(){
        MongoAccount mongoAccount = mongoAccountRepository.findById("5c53bfa846e356252cdf8400").get();
    }

    @ApiOperation(value = "测试获取的链接")
    @GetMapping(value = "/hbase")
    public void hbase() {
//        System.setProperty("hadoop.home.dir", hadoopDir);
        Configuration configuration = HBaseConfiguration.create();

//        configuration.set(HConstants.ZOOKEEPER_QUORUM, quorum);
//        configuration.set(HConstants.ZOOKEEPER_CLIENT_PORT, port);

        configuration.set(HConstants.ZOOKEEPER_QUORUM, quorum);
        try {
            org.apache.hadoop.hbase.client.Connection connection =
                                                                 ConnectionFactory.createConnection(configuration);
            Table table = connection.getTable(TableName.valueOf(tableName));
            log.info("获取到连接" + table.getName());
            ResultScanner results = table.getScanner(new Scan());
            for (Result result : results) {
                log.info("rowkey: " + Bytes.toLong(result.getRow()));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
