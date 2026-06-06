package com.learn.order.cmp;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

// LiteFlow 组件：校验库存
@LiteflowComponent("stockCheck")
public class StockCheckCmp extends NodeComponent {

    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        // getContextBean = 拿共享上下文，链上所有组件共用同一个实例
        System.out.println("[审核链] 第1步 — 校验库存：" + ctx.getProduct());
        ctx.appendResult("库存充足");
    }
}
