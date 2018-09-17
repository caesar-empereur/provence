package com.app.controller;

import com.zaxxer.hikari.hibernate.HikariConnectionProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    @ApiOperation(value = "链接")
    @GetMapping(value = "/hikari")
    public void connection() {
        HikariConnectionProvider provider = new HikariConnectionProvider();
        try {
            Connection connection = provider.getConnection();
            ResultSet
                    rs = connection.createStatement().executeQuery(" select * from user");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                log.info(id + " " + name);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
