package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.AttrVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品属性
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryAttrsByCid(QueryCondition condition, Long cid, Integer type);

    void saveAttr(AttrVo attrVo);
}

