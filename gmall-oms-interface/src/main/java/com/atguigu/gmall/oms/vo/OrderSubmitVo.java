package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVo {

    private String orderToken; // 防重
    private MemberReceiveAddressEntity address;
    private Integer payType;
    private String deliveryCompany;
    private List<OrderItemVo> orderItems;
    private Integer bounds;
    private BigDecimal totalPrice; // 校验价格
    private Long userId;
}
