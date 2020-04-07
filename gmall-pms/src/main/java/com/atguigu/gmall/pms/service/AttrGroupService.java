package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 属性分组
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo queryGroupByPage(QueryCondition condition, Long catId);

    GroupVo queryGroupWithAttrsByGid(Long gid);

    List<GroupVo> queryGroupWithAttrsByCid(Long cid);

    List<ItemGroupVo> queryItemGroupVoByCidAndSpuId(Long cid, Long spuId);
}

