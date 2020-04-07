package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVo;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {

    private List<MemberReceiveAddressEntity> addresses; // 收货信息

    private List<OrderItemVo> orderItems; // 商品信息

    private Integer bounds; // 购物积分

    private String orderToken; // 订单唯一标识，防止重复提交
}
