package com.ojbe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ojbe.mapper")
@EnableScheduling
@EnableCaching
public class DailwApplication {

    public static void main(String[] args) {
        SpringApplication.run(DailwApplication.class, args);
    }

}
