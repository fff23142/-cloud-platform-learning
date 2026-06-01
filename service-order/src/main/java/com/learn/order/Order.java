package com.learn.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

// 定义一个"订单"实体长什么样：id、userId、product、amount、deleted
@TableName("order_table")   // 表名叫 order_table，因为 order 是 SQL 保留字不能当表名
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;      // 下单用户的 id，通过这个 id 跨服务查用户名

    private String product;   // 商品名

    private Double amount;    // 金额

    @TableLogic    // 删数据时不真删，把 deleted 标记为 1
    private Integer deleted;

    // 无参构造 — 框架必须要
    public Order() {
    }

    // 有参构造 — 方便 new Order(1L, 1L, "鼠标", 199.0)
    public Order(Long id, Long userId, String product, Double amount) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", product='" + product + '\'' +
                ", amount=" + amount +
                '}';
    }
}
