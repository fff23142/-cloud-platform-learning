package com.learn.user;

// interface = 只定义方法签名，不写具体实现（C 里类似 .h 头文件声明）
// extends BaseMapper<User> = 继承 MyBatis-Plus 提供的基础增删改查
// 效果相当于免费获得：insert, deleteById, updateById, selectById, selectList 等方法
// 不用写一行 SQL，MyBatis-Plus 会自动生成

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// @Mapper 是注解（annotation）
// 理解为给 Spring 贴了一张标签："这是数据库操作接口，帮我管着"
// 不加这个注解的话，Spring 不知道这个接口的存在，运行时会报错

public interface UserMapper extends BaseMapper<User> {
    // 暂时空着，BaseMapper 已经自带最常用的方法了
    // 等以后需要复杂 SQL 的时候（比如多表联查），在这里加方法就行
}
