package com.learn.auth;

import com.learn.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 认证服务控制器：处理登录请求，验证用户身份，签发 JWT 令牌
// 跨域统一由网关 CorsConfig 处理，这里不需要重复加
@RestController
@RequestMapping("/auth")
public class UserLoginController {

    @Autowired
    private AuthenticationManager authManager;
    // 认证管理器 —— Spring Security 的"公章"
    // 拿着用户名密码去它那盖章，通过就是合法用户，不通过就是假的

    @Autowired
    private JwtUtil jwtUtil;

    // ====== 登录接口 ======
    // POST /auth/login  +  JSON body: {"username":"admin","password":"123456"}
    // 成功返回 Token，之后所有请求在 Header 里带上 Authorization: Bearer <token>

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            // 第1步：校验用户名密码
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 第2步：取出角色，去掉 Spring Security 自动加的 "ROLE_" 前缀
            String role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("user");

            // 第3步：生成 JWT Token（带角色）
            String token = jwtUtil.generateToken(username, role);

            // 第4步：返回 Token
            Map<String, String> data = Map.of(
                    "token", token,
                    "username", username,
                    "role", role
            );
            return Result.ok(data);

        } catch (Exception e) {
            return Result.fail(401, "用户名或密码错误");
        }
    }

    // ====== 查当前用户身份（需要带 Token 才能访问） ======
    // GET /auth/me  +  Header: Authorization: Bearer <token>
    // 这个接口在 AuthConfig 里被保护了，必须登录才能调

    @GetMapping("/me")
    public Result<String> currentUser(@RequestHeader("Authorization") String authHeader) {
        // Authorization 头的格式是 "Bearer eyJhbG..."
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsername(token);
        return Result.ok("当前登录用户：" + username);
    }
}
