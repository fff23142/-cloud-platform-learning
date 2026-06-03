package com.learn.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Security 的"交通规则"：
// 哪些路可以随便走（/auth/login）？哪些必须带 Token 才能走？
// @Configuration = 这个类里定义的都是配置规则，Spring 启动时会自动执行
// @EnableWebSecurity = 启用安全框架
@Configuration
@EnableWebSecurity
public class AuthConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private AuthUserMapper authUserMapper;

    // ====== 1. 密码加密器 ======
    // 密码绝不能明文存数据库。BCrypt 是一种单向哈希算法
    // 同一个密码每次加密结果都不同（带随机盐），但验证明文时能对上
    // C 语言类比：不能直接 strcmp 密码，而是用类似 crypt() 的函数

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ====== 2. 用户来源（从数据库查） ======
    // 实现 UserDetailsService 接口，告诉 Spring Security "用户存在哪、怎么查"
    // 内存版：InMemoryUserDetailsManager —— 重启就丢，现在已废弃
    // 数据库版：自己实现 loadUserByUsername 方法，从 user 表查

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 从数据库查用户
            AuthUser dbUser = authUserMapper.findByUsername(username);
            if (dbUser == null) {
                throw new RuntimeException("用户不存在：" + username);
            }
            // 把数据库查到的 AuthUser 转成 Spring Security 认识的 User 对象
            return User.builder()
                    .username(dbUser.getName())
                    .password(dbUser.getPassword())
                    .roles(dbUser.getRole())                // 从数据库取真实角色
                    .build();
            // Spring Security 会自动拿输入的明文密码跟数据库密文比对
            // 比对用的就是上面定义的 BCryptPasswordEncoder
        };
    }

    // ====== 3. 认证管理器 ======
    // Spring Security 的"公章"——登录时用它来校验用户名密码是否匹配

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // ====== 4. 安全规则（最重要的部分） ======
    // SecurityFilterChain = 一个请求进来后被什么规则拦截、拦截后又经过什么过滤器

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())           // API 服务用不上 CSRF 防护，关掉
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // STATELESS = 服务端不记登录状态，每次请求只看 Token
                // 传统网站的 session（服务端记登录态）跟微服务不搭
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register").permitAll()   // 登录、注册允许匿名访问
                .anyRequest().authenticated()                 // 其他所有请求都要登录
            )
            .addFilterBefore(jwtAuthFilter,
                    UsernamePasswordAuthenticationFilter.class);
            // 把我们自己的 JWT 过滤器插在 Spring 默认的用户名密码过滤器前面
            // 这样请求先被 JwtAuthFilter 处理：提取 Token → 验证 → 标记登录状态
        return http.build();
    }
}
