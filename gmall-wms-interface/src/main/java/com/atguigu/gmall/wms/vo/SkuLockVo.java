package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVo {

    private Long wareSkuId; // 锁定库存Id
    private Long skuId;
    private Integer count;
    private Boolean lock; // 商品锁定状态
    private String orderToken; //订单编号

}
