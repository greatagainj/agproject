package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性&属性分组关联
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageVo queryPage(QueryCondition params);

    void deleteAttrRelation(List<AttrAttrgroupRelationEntity> relationEntityList);
}

