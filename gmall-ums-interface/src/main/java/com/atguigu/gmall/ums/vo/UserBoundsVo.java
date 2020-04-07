package com.atguigu.gmall.ums.vo;

import lombok.Data;

@Data
public class UserBoundsVo {

    private Long memberId;

    private Integer growth; //成长积分

    private Integer integration; // 赠送积分
}
