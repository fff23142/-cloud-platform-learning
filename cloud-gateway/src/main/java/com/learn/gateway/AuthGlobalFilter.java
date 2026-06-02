package com.learn.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// 网关门卫：每个请求进来先在这里检查有没有合法 Token
// GlobalFilter = 对所有路由生效，不需要每个路由单独配置
// Ordered = 可以控制这个过滤器在过滤器链中的顺序
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final SecretKey key;

    public AuthGlobalFilter(@Value("${auth.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(Base64.getEncoder().encode(secret.getBytes()));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // OPTIONS 预检请求直接放行（浏览器跨域时自动发的）
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        // 登录接口直接放行（不用带 Token）
        if (path.startsWith("/auth/login")) {
            return chain.filter(exchange);
        }

        // 检查 Authorization 头
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return unAuth(exchange, "未提供 Token，请先登录");
        }

        // 验证 Token + 提取身份信息
        String token = header.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 从 Token 取出用户名和角色，放到自定义 Header 里传给下游服务
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // HTTP Header 不能直接传中文，先 URL 编码，下游服务收到后解码
            String encodedName = URLEncoder.encode(username, StandardCharsets.UTF_8);
            exchange.getRequest().mutate()
                    .header("X-User-Name", encodedName)
                    .header("X-User-Role", role != null ? role : "user");

            return chain.filter(exchange);
        } catch (Exception e) {
            return unAuth(exchange, "Token 无效或已过期");
        }
    }

    // 返回 401 错误 + JSON 提示
    private Mono<Void> unAuth(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + msg + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;    // 数字越小越先执行，-100 保证在路由转发之前执行
    }
}
