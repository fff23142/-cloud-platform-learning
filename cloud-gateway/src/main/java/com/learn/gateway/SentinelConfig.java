package com.learn.gateway;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

// Sentinel 限流配置——用最基础的方式设置 QPS 限制
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("user-hello");    // 资源名
        rule.setCount(2);                 // 每个统计窗口最多 2 个请求
        rule.setGrade(1);                 // QPS 模式
        rule.setLimitApp("default");
        rule.setControlBehavior(0);       // 0=直接拒绝, 1=Warm Up, 2=匀速排队
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
        System.out.println("[Sentinel] 限流规则已加载：user-hello 限制 2 QPS");
    }

    // Sentinel 入口包装——返回 true 表示通过了，false 表示被限流
    public static boolean allow(String resource) {
        try (Entry entry = SphU.entry(resource)) {
            return true;
        } catch (BlockException e) {
            return false;
        }
    }
}
