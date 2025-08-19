package com.dailw;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.dailw.mapper")
@EnableScheduling
public class DailwApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailwApplication.class, args);
    }

}
