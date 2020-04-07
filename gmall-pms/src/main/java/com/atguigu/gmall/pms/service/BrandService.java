package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 品牌
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface BrandService extends IService<BrandEntity> {

    PageVo queryPage(QueryCondition params);
}

