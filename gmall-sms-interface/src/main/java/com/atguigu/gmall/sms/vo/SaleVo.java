package com.atguigu.gmall.sms.vo;

import lombok.Data;

@Data
public class SaleVo {
    // 0-优惠券    1-满减    2-阶梯
    private String type;

    private String desc;//促销信息/优惠券的名字
}
