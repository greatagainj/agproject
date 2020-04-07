package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageVo queryPage(QueryCondition params) {

        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryGroupByPage(QueryCondition condition, Long catId) {
        QueryWrapper wrapper = new QueryWrapper<AttrGroupEntity>();
        if (catId != null) {
            wrapper.eq("catelog_id", catId);
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public GroupVo queryGroupWithAttrsByGid(Long gid) {

        GroupVo groupVo = new GroupVo();
        // 1、查询group
        AttrGroupEntity groupEntity = this.getById(gid);
        BeanUtils.copyProperties(groupEntity,groupVo);

        // 2、查询 组-属性 关联关系
        List<AttrAttrgroupRelationEntity> relations = this.relationDao.
                selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));
        if (CollectionUtils.isEmpty(relations)) {
            return groupVo;
        } else {
            groupVo.setRelations(relations);
        }
        // 3、根据关联关系查询组下属性
        // 获取list中某个字段的list
        List<Long> attrIds = relations.stream().map(realationEntity -> realationEntity.getAttrId()).collect(Collectors.toList());
        List<AttrEntity> attrEntities = this.attrDao.selectBatchIds(attrIds);
        groupVo.setAttrEntities(attrEntities);

        return groupVo;
    }

    @Override
    public List<GroupVo> queryGroupWithAttrsByCid(Long cid) {

        // 根据cid查询三级分类下的所有属性分组
        List<AttrGroupEntity> attrGroupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        // 根据分组中的id查询中间表
        // 根据中间表AttrIds查询参数
        // 数据类型转换
        List<GroupVo> groupVos = attrGroupEntityList.stream().map(attrGroupEntity ->
                this.queryGroupWithAttrsByGid(attrGroupEntity.getAttrGroupId())).collect(Collectors.toList());
        return groupVos;
    }

    @Override
    public List<ItemGroupVo> queryItemGroupVoByCidAndSpuId(Long cid, Long spuId) {

        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));

        List<ItemGroupVo> itemGroupVos = attrGroupEntities.stream().map(group -> {
            ItemGroupVo itemGroupVo = new ItemGroupVo();

            itemGroupVo.setName(group.getAttrGroupName());

            // 查询规格参数和值
            List<AttrAttrgroupRelationEntity> relationEntities = this.relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", group.getAttrGroupId()));
              // 从中间表找到属性id
            List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
              // 通过属性id和spuid找到属性值
            List<ProductAttrValueEntity> attrValueEntities = this.productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId).in("attr_id", attrIds));

            itemGroupVo.setBaseAttrs(attrValueEntities);


            return itemGroupVo;
        }).collect(Collectors.toList());

        return itemGroupVos;
    }

}