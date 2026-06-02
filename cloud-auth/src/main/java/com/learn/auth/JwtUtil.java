package com.learn.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

// JWT 工具类：负责签发票据和验证票据
// JWT = JSON Web Token，你可以理解为一张"电子通行证"
// 用户登录后服务端签一张给他，之后每次请求带着它，服务端验证通过才允许访问
@Component      // Spring 管理的组件，别的地方用 @Autowired 拿
public class JwtUtil {

    private final SecretKey key;

    // @Value 把 application.yml 里的配置值注入进来
    // 相当于你在 C 里读配置文件然后赋值给全局变量
    public JwtUtil(@Value("${auth.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secret.getBytes()));
    }

    @Value("${auth.jwt.expiration}")
    private long expiration;

    // ====== 生成 Token ======
    // 登录成功后调用，把用户名和角色写进 Token

    public String generateToken(String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)                   // Token 里存用户名
                .claim("role", role)                 // 多加一个自定义字段：角色
                .issuedAt(now)                      // 签发时间
                .expiration(new Date(now.getTime() + expiration))  // 过期时间
                .signWith(key)                      // 用密钥签名（防篡改）
                .compact();                         // 压缩成字符串
    }

    // ====== 从 Token 里提取用户名 ======

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    // ====== 从 Token 里提取角色 ======

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    // ====== 验证 Token 是否有效 ======
    // 签名不对、过期了都会抛异常

    public boolean validate(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 解析 Token 的内部方法
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
