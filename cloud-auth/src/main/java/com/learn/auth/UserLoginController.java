package com.learn.auth;

import com.learn.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 认证服务控制器：登录、注册、身份查询
// 跨域统一由网关 CorsConfig 处理
@RestController
@RequestMapping("/auth")
public class UserLoginController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthUserMapper authUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsername(token);
        return Result.ok("当前登录用户：" + username);
    }

    // ====== 注册接口 ======
    // 未登录时：只能注册普通用户
    // 管理员登录后：可以注册管理员（在请求体里加 {"role":"admin"}）

    @PostMapping("/register")
    public Result<String> register(@RequestBody Map<String, String> body,
                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = body.get("username");
        String password = body.get("password");
        String confirmPassword = body.get("confirmPassword");
        String email = body.getOrDefault("email", "");
        String wantedRole = body.getOrDefault("role", "user");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.fail(400, "用户名和密码不能为空");
        }
        if (confirmPassword != null && !confirmPassword.isBlank()
                && !password.equals(confirmPassword)) {
            return Result.fail(400, "两次输入的密码不一致");
        }

        if (authUserMapper.findByUsername(username) != null) {
            return Result.fail(409, "用户名「" + username + "」已存在，请换一个");
        }

        // 权限判断：只有已登录的管理员才能注册 admin 账号
        String finalRole = "user";
        if ("admin".equals(wantedRole)) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.fail(403, "只有管理员才能创建管理员账号，请先登录");
            }
            String token = authHeader.replace("Bearer ", "");
            if (!jwtUtil.validate(token)) {
                return Result.fail(403, "Token 无效或已过期");
            }
            String currentRole = jwtUtil.getRole(token);
            if (!"admin".equals(currentRole)) {
                return Result.fail(403, "权限不足：只有管理员才能创建管理员账号");
            }
            finalRole = "admin";
        }

        String encoded = passwordEncoder.encode(password);
        authUserMapper.register(username, email, encoded, finalRole);
        return Result.ok("注册成功！" + username + "（" + finalRole + "）");
    }

    // ====== 修改密码 ======
    // POST /auth/change-password  +  Header: Authorization: Bearer <token>
    // JSON: {"oldPassword":"旧密码","newPassword":"新密码","confirmPassword":"新密码确认"}

    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> body,
                                         @RequestHeader("Authorization") String authHeader) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (oldPassword == null || newPassword == null || confirmPassword == null) {
            return Result.fail(400, "请填写所有密码字段");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.fail(400, "两次输入的新密码不一致");
        }
        if (newPassword.length() < 6) {
            return Result.fail(400, "新密码至少 6 位");
        }

        // 从 Token 取当前用户名
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsername(token);

        // 验证旧密码
        AuthUser dbUser = authUserMapper.findByUsername(username);
        if (dbUser == null || !passwordEncoder.matches(oldPassword, dbUser.getPassword())) {
            // passwordEncoder.matches(明文, 密文) = 比对密码是否正确
            // 类似 C 里 strcmp(crypt(input, salt), stored_hash)
            return Result.fail(401, "旧密码不正确");
        }

        // 更新密码
        String encoded = passwordEncoder.encode(newPassword);
        authUserMapper.updatePassword(username, encoded);
        return Result.ok("密码修改成功！");
    }
}
