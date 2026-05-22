package com.example.kgplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {Neo4jReactiveDataAutoConfiguration.class})
@MapperScan("com.example.kgplatform.mapper")
@EnableAsync
public class KgPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgPlatformApplication.class, args);
    }
}
