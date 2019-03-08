package com.app.controller;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Resource;

import com.app.repository.mongodb.MongoAccountRepository;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.model.hbase.HbaseAccount;
import com.app.model.mongodb.MongoAccount;
import com.app.repository.hbase.AccountRepository;

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
    private AccountRepository accountRepository;
    
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
        HbaseAccount hbaseAccount = new HbaseAccount();
        hbaseAccount.setCreateTime(new Date());
        hbaseAccount.setBalance(10.00);
        hbaseAccount.setUsername("heheda");
        hbaseAccount.setId(UUID.randomUUID().toString().replace("-",""));
        accountRepository.save(hbaseAccount);
    }

}
