package com.caq.mall.thirdservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MallThirdServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallThirdServiceApplication.class, args);
    }

}
