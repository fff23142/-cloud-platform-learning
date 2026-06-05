package com.learn.order;

import com.learn.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

// 声明要远程调用 user 服务的哪些方法，Feign 自动生成 HTTP 客户端实现
// C 语言类比：想跨进程调函数，你得写 socket connect + send + recv + 手动解析
// Java 这边定义一个 interface，Feign 替你写好了所有网络通信、序列化代码
@FeignClient("service-user")    // 去 Nacos 找名为 service-user 的服务
public interface UserFeignClient {

    @GetMapping("/user/{id}")
    Result<UserDTO> getUserById(@PathVariable Long id);

    @PostMapping("/user/deduct-balance")     // 对应 user 服务的扣款接口
    Result<String> deductBalance(@RequestBody Map<String, Object> body);
}
