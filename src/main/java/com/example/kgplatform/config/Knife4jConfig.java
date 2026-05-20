package com.example.kgplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KG Platform API")
                        .description("知识图谱管理平台接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("KG Platform Team")));
    }
}
