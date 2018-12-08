package com.app.controller;

import com.app.model.mysql.User;
import com.app.repository.mysql.UserRepository;
import com.app.vo.mysql.UserView;
import com.zaxxer.hikari.hibernate.HikariConnectionProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/9/17.
 */
@Api(description = "guest接口")
@RestController
@RequestMapping("/connection")
@Slf4j
public class ConnectionController {
    
    @Value("${hbase.zookeeper.quorum}")
    private String quorum;
    
    @Value("${hbase.zookeeper.port}")
    private String port;
    
    @Value("${table.name}")
    private String tableName;
    
    @Value("${hadoop.dir}")
    private String hadoopDir;
    
    @Resource
    private UserRepository userRepository;
    
    @ApiOperation(value = "jpa测试")
    @PostMapping(value = "/jpa")
    public void jpa(@RequestBody UserView userView) {
        User user = new User();
        BeanUtils.copyProperties(userView, user);
        userRepository.save(user);
    }
    
    @ApiOperation(value = "测试获取的链接")
    @GetMapping(value = "/hikari")
    public void connection() {
        Map<String, String> map = new HashMap<>();
        map.put(AvailableSettings.DRIVER, "");
        map.put(AvailableSettings.URL,
                "jdbc:mysql://127.0.0.1:3306/hbase?useUnicode=true&characterEncoding=UTF-8");
        map.put(AvailableSettings.USER, "root");
        map.put(AvailableSettings.PASS, "123456");
        map.put(AvailableSettings.DRIVER, "com.mysql.jdbc.Driver");
        
        HikariConnectionProvider provider = new HikariConnectionProvider();
        provider.configure(map);
        try {
            Connection connection = provider.getConnection();
            ResultSet rs = connection.createStatement().executeQuery("select * from user");
            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("username");
                log.info(id + " " + name);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @ApiOperation(value = "测试获取的链接")
    @GetMapping(value = "/hbase")
    public void hbase() {
        System.setProperty("hadoop.home.dir", hadoopDir);
        Configuration configuration = HBaseConfiguration.create();
        configuration.set(HConstants.ZOOKEEPER_QUORUM, quorum);
        
        configuration.set(HConstants.ZOOKEEPER_CLIENT_PORT, port);
        try {
            org.apache.hadoop.hbase.client.Connection connection =
                                                                 ConnectionFactory.createConnection(configuration);
            Table monitorTable = connection.getTable(TableName.valueOf(tableName));
            ResultScanner results = monitorTable.getScanner(new Scan());
            for (Result result : results) {
                log.info("rowkey: " + Bytes.toLong(result.getRow()));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
