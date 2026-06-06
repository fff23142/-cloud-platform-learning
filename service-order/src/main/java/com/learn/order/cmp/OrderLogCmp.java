package com.learn.order.cmp;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

// LiteFlow 组件：记录订单日志
@LiteflowComponent("orderLog")
public class OrderLogCmp extends NodeComponent {

    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        System.out.println("[审核链] 第3步 — 记录日志");
        ctx.appendResult("订单审核通过！");
    }
}
