package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * sku销售属性&值
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageVo queryPage(QueryCondition params);

    List<SkuSaleAttrValueEntity> queryAttrValueEntities(Long spuId);
}

