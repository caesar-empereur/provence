package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hbase.annotation.HbaseTableScan;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author yingyang
 * @date 2018/7/5.
 */
@ComponentScan({ "com.app"})
@EntityScan("com.app.model")
@SpringBootApplication
@EnableMongoRepositories("com.app.repository.mongodb")
@EnableSwagger2
@ServletComponentScan
@HbaseTableScan(modelPackage = "com.app.model.hbase", repositoryPackage = "com.app.repository.hbase")
public class HbaseApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
//        SpringApplication.run(HbaseApplication.class);
        System.out.println(hjehgf());
    }

    private static String hjehgf(){
        String s ;
        try {
            s = 2/2 + "";
        }
        catch (Exception e) {
            throw new RuntimeException("Exception");
        }
        finally {
            System.out.println("finally");
        }
        System.out.println("after finally");
        return s;
    }
}
