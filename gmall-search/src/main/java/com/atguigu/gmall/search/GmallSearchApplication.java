package com.atguigu.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GmallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallSearchApplication.class, args);
    }

}
