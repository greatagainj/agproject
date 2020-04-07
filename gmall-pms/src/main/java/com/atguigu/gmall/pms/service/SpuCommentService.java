package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SpuCommentEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品评价
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageVo queryPage(QueryCondition params);
}

