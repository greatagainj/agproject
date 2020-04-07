package com.atguigu.gmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GmallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallAuthApplication.class, args);
    }

}
