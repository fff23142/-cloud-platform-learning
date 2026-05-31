package com.learn.user;

// import = C 里的 #include，把要用到的外部类引进来
import com.learn.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*") // 允许任何来源的网页调用此接口（本地开发用，生产环境要收紧）
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired  // Spring 会自动把 UserMapper 的实现注入进来，不需要手动 new
    private UserMapper userMapper;

    // ====== 1. 最简单的文本接口 ======
    // 验证服务是否正常启动

    @GetMapping("/hello")   // 处理 GET 请求，路径为 /user/hello
    public String hello() {
        return "Hello, service-user is running!";
        // 浏览器输入 http://localhost:8091/user/hello 能看到这句话
    }

    // ====== 2. 查所有用户（从数据库） ======

    @GetMapping("/list")
    public List<User> list() {
        // selectList(null) = 查整张表，相当于 SQL: SELECT * FROM user WHERE deleted=0
        // 参数 null 表示没有过滤条件，MyBatis-Plus 自动过滤逻辑删除字段
        return userMapper.selectList(null);
        // 浏览器输入 http://localhost:8091/user/list 看 JSON
    }

    // ====== 3. 查单个用户（通过 ID，路径参数） ======
    // URL 示例: /user/1  →  查 id=1 的用户

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

    // ====== 4. 新增用户（POST） ======

    @PostMapping("/add")
    public Result<User> add(@RequestBody User user) {
        // @RequestBody = 把 HTTP 请求体里的 JSON 自动转成 User 对象
        // 跟 C 里你手动解析字符串再给 struct 字段赋值一个意思，Java 自动做了

        userMapper.insert(user);
        // insert = MyBatis-Plus 自带的插入方法，相当于 SQL: INSERT INTO user (name, email) VALUES (?, ?)
        // 执行完后 user 对象的 id 字段会被自动回填（数据库自增主键）

        return Result.ok(user);
        // 把插入成功的用户数据返回来，带上数据库生成的 id
    }
}
