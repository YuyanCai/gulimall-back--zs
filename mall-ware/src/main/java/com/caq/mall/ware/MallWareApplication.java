package com.caq.mall.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient //标注启动服务发现
public class MallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallWareApplication.class, args);
    }

}
