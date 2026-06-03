package com.learn.auth;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

// 从数据库 user 表查/写登录用户
@Mapper
public interface AuthUserMapper {

    @Select("SELECT name, password, role FROM user WHERE name = #{name} AND deleted = 0")
    AuthUser findByUsername(@Param("name") String name);

    @Insert("INSERT INTO user (name, email, password, role) VALUES (#{name}, #{email}, #{password}, #{role})")
    int register(@Param("name") String name,
                 @Param("email") String email,
                 @Param("password") String password,
                 @Param("role") String role);

    @Update("UPDATE user SET password = #{newPassword} WHERE name = #{name} AND deleted = 0")
    int updatePassword(@Param("name") String name, @Param("newPassword") String newPassword);
}
