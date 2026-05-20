package com.example.kgplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.kgplatform.mapper")
@EnableAsync
public class KgPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgPlatformApplication.class, args);
    }
}
