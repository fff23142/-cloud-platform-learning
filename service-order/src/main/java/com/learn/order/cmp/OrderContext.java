package com.learn.order.cmp;

// LiteFlow 上下文——组件之间共享数据的"黑板书"
// 每个组件执行时可以读写这个对象，链上所有组件共享同一个实例
public class OrderContext {
    private String product;
    private String result = "";

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public void appendResult(String step) {
        this.result += step + " → ";
    }
}
