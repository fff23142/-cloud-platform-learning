package com.learn.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

// 从数据库 user 表查登录用户
// 用 @Select 直接写 SQL，而不是 MyBatis-Plus 的 BaseMapper
// 因为这里只需要一个查用户名的方法，不需要全套 CRUD
@Mapper
public interface AuthUserMapper {

    @Select("SELECT name, password, role FROM user WHERE name = #{name} AND deleted = 0")
    AuthUser findByUsername(@Param("name") String name);
    // #{name} = MyBatis 参数占位符，会自动防 SQL 注入
    // 相当于 C 里的参数化查询：stmt = prepare("SELECT ... WHERE name = ?")
}
