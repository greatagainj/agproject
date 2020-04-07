package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * spu属性值
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageVo queryPage(QueryCondition params);

    List<ProductAttrValueEntity> querySearchAttrValueBySpuId(Long supId);
}

