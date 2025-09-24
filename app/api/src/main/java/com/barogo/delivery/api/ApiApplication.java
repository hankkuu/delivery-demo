package com.barogo.delivery.api;

import com.barogo.delivery.Constant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = Constant.APPLICATION_JPA_REPOSITORY)
@EntityScan(basePackages = Constant.APPLICATION_JPA_ENTITY)
@SpringBootApplication(scanBasePackages = {Constant.APPLICATION_BASE_PACKAGE})
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}