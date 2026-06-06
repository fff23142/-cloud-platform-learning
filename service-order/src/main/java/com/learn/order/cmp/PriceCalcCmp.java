package com.learn.order.cmp;

import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;

// LiteFlow 组件：计算价格
@LiteflowComponent("priceCalc")
public class PriceCalcCmp extends NodeComponent {

    @Override
    public void process() {
        OrderContext ctx = this.getContextBean(OrderContext.class);
        System.out.println("[审核链] 第2步 — 计算价格");
        ctx.appendResult("价格：99.00（已打折）");
    }
}
