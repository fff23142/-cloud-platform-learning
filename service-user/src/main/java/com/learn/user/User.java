package com.learn.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
//定义一个"用户"实体长什么样：id、name、email、deleted
// package 就是 C 语言里的文件夹路径，用来组织代码不重名
// 这个文件的完整名字其实是 com.learn.user.User

@TableName("user")     // 告诉 MyBatis-Plus 这个类对应 MySQL 里哪张表
public class User {
    // Java: 类
    // C 类比: struct

    // ====== 属性（C: struct 里面的字段）======

    @TableId(type = IdType.AUTO)    // 主键，自增
    private Long id;
    // private = 外面不能直接 user.id 访问，要通过 get/set 方法
    // Long 是包装类型，可以存 null；如果写 long 则是原始类型，默认 0

    private String name;
    // String 是 Java 内置的字符串类，不像 C 是 char[]

    private String email;

    private Double balance;     // 账户余额，Seata 分布式事务演示用

    @TableLogic
    private Integer deleted;

    // ====== 构造方法（C: 没有直接对应，类似初始化函数）======

    // 无参构造 — Spring 框架要求必须要有，否则自动映射会报错
    public User() {
    }

    // 有参构造 — 方便直接 new User(1L, "张三", "zhang@example.com")
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        // this.xxx = 成员变量
        // 右边的 xxx = 参数
        // 因为同名，默认离得近的是参数，用 this 指回 "这个对象的" 成员
    }

    // ====== getter / setter ======
    // Java 习惯用 getXxx / setXxx 而不是直接暴露字段
    // IDEA 可以自动生成：右键 → Generate → Getter and Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }

    // 调试用，把对象转成可读字符串，类似 C 里自己写 print_user() 函数
    @Override // 表示重写了父类的方法
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
