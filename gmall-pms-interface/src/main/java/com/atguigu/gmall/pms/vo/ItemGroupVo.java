package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * 基本属性分组及组下的规格参数
 */
@Data
public class ItemGroupVo {

    private String name;//分组的名字
    private List<ProductAttrValueEntity> baseAttrs;
}
