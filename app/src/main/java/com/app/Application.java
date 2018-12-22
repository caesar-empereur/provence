package com.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hbase.core.HtableScanHandler;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author yingyang
 * @date 2018/7/5.
 */
@ComponentScan({"com.app","com.hbase"})
@EntityScan("com.app.model")
@SpringBootApplication
@EnableSwagger2
@ServletComponentScan
public class Application implements WebMvcConfigurer {

    @Bean
    public HtableScanHandler htableScanHandler(){
        return new HtableScanHandler("com.app.model");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
