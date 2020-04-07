package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:31:34
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    public int updateCloseOrder(String orderToken);

    public int payOrder(String orderToken);
}
