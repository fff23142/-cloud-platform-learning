package com.learn.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
// 整个user程序模块的启动入口，好比是c语言的main()函数
@SpringBootApplication
@EnableDiscoveryClient   // 向 Nacos 注册此服务
@EnableFeignClients      // 允许用 Feign 调用其他微服务
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
