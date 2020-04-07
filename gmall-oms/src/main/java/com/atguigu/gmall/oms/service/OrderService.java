package com.atguigu.gmall.oms.service;

import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 订单
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:31:34
 */
public interface OrderService extends IService<OrderEntity> {

    PageVo queryPage(QueryCondition params);

    OrderEntity saveOrder(OrderSubmitVo orderSubmitVo);
}

