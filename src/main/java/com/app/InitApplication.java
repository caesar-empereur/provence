package com.app;

import com.app.annotation.HbaseTableScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan("com.app")
@EntityScan("com.app.model")
@HbaseTableScan("com.app.model")
@SpringBootApplication
@EnableSwagger2
@ServletComponentScan
@EnableScheduling
@EnableAutoConfiguration()
public class InitApplication implements WebMvcConfigurer {
    
    public static void main(String[] args) {
        SpringApplication.run(InitApplication.class);
    }
}
