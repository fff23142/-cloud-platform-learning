package com.learn.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

// 定义操作订单数据库的方法（查、增、删、改）
// interface 只声明方法，BaseMapper 白送了 insert/delete/update/select 全套
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    // 暂时空着，复杂查询以后再加
}
