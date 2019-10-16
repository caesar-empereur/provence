package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hbase.annotation.HbaseTableScan;

/**
 * @author yingyang
 * @date 2018/7/5.
 */
@ComponentScan({"com.app","com.hbase"})
@SpringBootApplication
@HbaseTableScan(modelPackage = "com.app", repositoryPackage = "com.app")
public class HbaseApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(HbaseApplication.class);
    }

}
