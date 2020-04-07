package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * spu属性值
 * 
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
@Mapper
public interface ProductAttrValueDao extends BaseMapper<ProductAttrValueEntity> {

    List<ProductAttrValueEntity> querySearchAttrValueBySpuId(Long supId);
}
