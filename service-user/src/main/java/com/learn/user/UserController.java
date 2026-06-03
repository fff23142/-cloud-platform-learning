package com.learn.user;

// import = C 里的 #include，把要用到的外部类引进来
import com.baomidou.dynamic.datasource.annotation.DS;
import com.learn.common.Result;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
//接收浏览器发来的 HTTP 请求，调Mapper代码根据网页需要读写数据库
//将结果包装为JSON返回给浏览器（跨域统一由网关 CorsConfig 处理）
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedissonClient redissonClient;
    // Redisson 是 Redis 的 Java 客户端，RLock 是基于 Redis 实现的分布式锁

    // ====== 1. 最简单的文本接口 ======
    // 验证服务是否正常启动

    @GetMapping("/hello")
    public String hello(
            @RequestHeader(value = "X-User-Name", defaultValue = "%E6%9C%AA%E7%9F%A5") String username,
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        // 网关把中文名 URL 编码了，这里解码还原
        username = URLDecoder.decode(username, StandardCharsets.UTF_8);
        return "Hello, " + username + "！ 你的角色是：" + role;
    }

    // ====== 1.5 管理员专用接口（接口权限演示） ======

    @GetMapping("/admin")
    public Result<String> adminOnly(
            @RequestHeader(value = "X-User-Role", defaultValue = "user") String role) {
        if (!"admin".equals(role)) {
            return Result.fail(403, "权限不足，仅管理员可访问");
        }
        return Result.ok("欢迎管理员！这是只有 admin 才能看到的页面");
        // 普通用户访问会收到 403
    }

    // ====== 2. 查所有用户（从从库读取） ======

    @DS("slave")    // 标记走从库——只读查询不应该给主库压力
    @GetMapping("/list")
    public List<User> list() {
        // selectList(null) = 查整张表，相当于 SQL: SELECT * FROM user WHERE deleted=0
        // 参数 null 表示没有过滤条件，MyBatis-Plus 自动过滤逻辑删除字段
        return userMapper.selectList(null);
        // 浏览器输入 http://localhost:8091/user/list 看 JSON
    }

    // ====== 3. 查单个用户（从从库读取） ======

    @DS("slave")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        // @PathVariable = 把 URL 里的 {id} 取出来赋给参数 id
        // 类似于 C 里从字符串解析出一个 long 值

        User user = userMapper.selectById(id);
        // selectById = 按主键查一条，相当于 SQL: SELECT * FROM user WHERE id=?

        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        return Result.ok(user);
        // 返回格式: {"code":200, "message":"success", "data":{...}}
    }

    // ====== 4. 新增用户（写入主库） ======

    @DS("master")   // 标记走主库——写操作必须走主库
    @PostMapping("/add")
    public Result<User> add(@RequestBody User user) {
        // @RequestBody = 把 HTTP 请求体里的 JSON 自动转成 User 对象
        // 跟 C 里你手动解析字符串再给 struct 字段赋值一个意思，Java 自动做了

        userMapper.insert(user);
        return Result.ok(user);
    }

    // ====== 5. 分布式锁演示（对比有锁/无锁） ======

    private int stock = 100;

    @GetMapping("/deduct-no-lock")
    public Result<String> deductNoLock() throws Exception {
        if (stock <= 0) return Result.fail(400, "库存不足");
        Thread.sleep(3000);                 // 故意卡3秒，放大并发问题
        stock--;
        return Result.ok("扣减成功，剩余库存：" + stock);
    }

    @GetMapping("/deduct-with-lock")
    public Result<String> deductWithLock() throws Exception {
        RLock lock = redissonClient.getLock("inventory-lock");
        if (!lock.tryLock(2, 10, java.util.concurrent.TimeUnit.SECONDS)) {
            return Result.fail(429, "系统繁忙（锁被占用），请稍后重试");
        }
        try {
            if (stock <= 0) return Result.fail(400, "库存不足");
            Thread.sleep(3000);
            stock--;
            return Result.ok("扣减成功，剩余库存：" + stock);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @GetMapping("/reset-stock")
    public Result<String> resetStock() {
        stock = 100;
        return Result.ok("库存已重置为 100");
    }
}
