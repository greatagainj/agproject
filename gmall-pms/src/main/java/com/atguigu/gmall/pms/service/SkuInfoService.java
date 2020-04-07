package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * sku信息
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageVo queryPage(QueryCondition params);
}

