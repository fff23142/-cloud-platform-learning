package com.learn.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

// JWT 认证过滤器：每个请求到达 Controller 之前，先在这里被拦截检查
// OncePerRequestFilter = 每个请求保证只经过一次，不会重复过滤
// C 语言类比：中间件/钩子函数，请求进来先调用你的检查函数
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // 第1步：从请求头取 Authorization
        String header = request.getHeader("Authorization");

        // 第2步：如果有且以 Bearer 开头，就验证
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.replace("Bearer ", "");

            if (jwtUtil.validate(token)) {
                // Token 有效 → 提取用户名，告诉 Spring Security "这个请求是合法用户发的"
                String username = jwtUtil.getUsername(token);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                username, null, Collections.emptyList()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
                // 这行是关键：把认证信息放进安全上下文
                // 之后 Controller 和 Service 都能拿到当前登录用户的身份
            }
        }

        // 第3步：不管有没有 Token，都放行交给下一个过滤器
        // 如果没有合法 Token，后面的 Spring Security 规则会拦截（返回 403）
        chain.doFilter(request, response);
    }
}
