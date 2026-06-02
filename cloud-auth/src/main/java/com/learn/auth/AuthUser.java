package com.learn.auth;

// 数据库查回来的用户数据简洁容器，只取认证需要的字段
public class AuthUser {
    private String name;
    private String password;
    private String role;        // admin 或 user

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
