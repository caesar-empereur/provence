package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hbase.annotation.HbaseTableScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author yingyang
 * @date 2018/7/5.
 */
@ComponentScan({ "com.app","com.hbase"})
@EntityScan("com.app.model.jpa")
@SpringBootApplication
@EnableSwagger2
@HbaseTableScan(modelPackage = "com.app.model.hbase", repositoryPackage = "com.app.repository.hbase")
public class HbaseApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(HbaseApplication.class);
    }

}
