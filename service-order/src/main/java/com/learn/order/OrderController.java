package com.learn.order;

import com.learn.common.Result;
import com.learn.order.cmp.OrderContext;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import io.seata.core.context.RootContext;
import io.seata.core.model.GlobalStatus;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 接收浏览器发来的 HTTP 请求，调 Mapper 读写订单数据
// 内部通过 Feign（UserFeignClient）远程调用 service-user 拿用户信息
// 跨域统一由网关 CorsConfig 处理
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private FlowExecutor flowExecutor;  // LiteFlow 流程执行器，传入链名即可触发整条链
    // 这就是 Feign 的关键：userFeignClient 看起来是个本地变量
    // 实际上每次调用都会发 HTTP 请求到 service-user，你不需要写一行网络代码

    // ====== 1. 查所有订单 ======

    @GetMapping("/list")
    public List<Order> list() {
        return orderMapper.selectList(null);
        // 浏览器打开 http://localhost:8092/order/list 看 JSON
    }

    // ====== 2. 查单个订单 + 用户信息（跨服务调用） ======

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        // 第一步：查订单
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.fail(404, "订单不存在");
        }

        // 第二步：通过 Feign 远程调 user 服务，获取下单用户的信息
        Result<UserDTO> userResult = userFeignClient.getUserById(order.getUserId());
        // 这行代码背后发生了什么：
        //   Feign 在 Nacos 找到 service-user 的地址 (192.168.xxx:8091)
        //   发 HTTP GET 请求到 http://192.168.xxx:8091/user/{id}
        //   拿到 JSON 后自动反序列化成 Result<UserDTO> 对象
        //   所有这些你只写了一行接口调用 —— 和调本地方法一样

        // 第三步：把订单和用户信息打包返回
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("user", userResult != null ? userResult.getData() : null);
        // HashMap 类似 C 的键值对数组，key 是字符串，value 可以是任何对象

        return Result.ok(result);
        // 浏览器打开 http://localhost:8092/order/1 看效果
        // 会同时返回订单详情 + 下单用户的姓名邮箱
    }

    // ====== 3. 下单 + 扣款（Seata 分布式事务演示） ======

    @GlobalTransactional(timeoutMills = 30000)
    @PostMapping("/place")
    public Result<String> placeOrder(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String product = body.get("product").toString();
        Double amount = Double.valueOf(body.get("amount").toString());

        // ① 插入订单（本服务）
        Order order = new Order(null, userId, product, amount);
        orderMapper.insert(order);

        // ② 远程扣余额（跨服务）
        try {
            Result<String> deductResult = userFeignClient.deductBalance(
                    Map.of("userId", userId, "amount", amount));

            if (deductResult == null || deductResult.getCode() != 200) {
                String msg = deductResult != null ? deductResult.getMessage() : "服务异常";
                throw new RuntimeException("扣款失败：" + msg);
            }
            return Result.ok("下单成功，订单ID=" + order.getId() + "，" + deductResult.getData());

        } catch (RuntimeException e) {
            // 扣款失败 → Seata 回滚订单插入
            return Result.fail(400, e.getMessage());
        }
    }

    // ====== 4. LiteFlow 规则链演示 ======
    // GET /order/audit?product=鼠标 → 触发完整的订单审核链

    @GetMapping("/audit")
    public Result<String> audit(String product) {
        OrderContext ctx = new OrderContext();
        ctx.setProduct(product);

        // 执行链：stockCheck → priceCalc → orderLog
        LiteflowResponse response = flowExecutor.execute2Resp(
                "orderCheckChain", null, ctx);

        if (response.isSuccess()) {
            return Result.ok(ctx.getResult());
        } else {
            return Result.fail(500, "审核失败：" + response.getMessage());
        }
    }

    // 统一的异常拦截
    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleException(RuntimeException e) {
        return Result.fail(400, e.getMessage());
    }
}
