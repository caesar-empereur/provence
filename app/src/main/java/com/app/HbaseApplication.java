package com.app;

import com.hbase.annotation.HbaseTableScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @author yingyang
 * @date 2018/7/5.
 */
@ComponentScan({ "com.app","com.hbase"})
@EntityScan("com.app.model.jpa")
@SpringBootApplication
//@EnableMongoRepositories("com.app.repository.mongodb")
@EnableSwagger2
@ServletComponentScan
@HbaseTableScan(modelPackage = "com.app.model.hbase", repositoryPackage = "com.app.repository.hbase")
public class HbaseApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(HbaseApplication.class);
    }

}
