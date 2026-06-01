package com.learn.order;

// 跨服务传输用的用户信息，只取需要的字段（跨服务调用的规矩：不直接依赖对方内部类）
// 定义远程服务返回的数据结构，用于接收 user 服务的 json 数据
public class UserDTO {
    private Long id;
    private String name;
    private String email;

    public UserDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "UserDTO{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
