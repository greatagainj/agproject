package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVo {
    // 顶部分类品牌
    private Long skuId;
    private CategoryEntity categoryEntity; //private Long catalogId;
    private BrandEntity brandEntity;// private Long brandId;
    private Long spuId;
    private String spuName;
    // 中间图片标题价格等
    private String skuTitle;
    private String skuSubtitle;
    private BigDecimal price;
    private BigDecimal weight;
    private List<SkuImagesEntity> pics;
    // 中间sku的所有促销信息
    private List<SaleVo> sales;
    // 中间是否有货
    private Boolean store;
    // 中间所有销售属性组合
    private List<SkuSaleAttrValueEntity> saleAttrs;

    // 中下所有基本属性组、规格参数
    private List<ItemGroupVo> attrGroups;

    //下部详情介绍
    private List<String> images; //private SpuInfoDescEntity desc;
}
