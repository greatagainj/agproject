package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;// 商品id
    private String title;// 标题
    private String defaultImage;// 图片
    private BigDecimal price;// 价格
    private Integer count;// 购买数量
    private BigDecimal weight;
    private Boolean store;
    private List<SkuSaleAttrValueEntity> saleAttrValues;// 销售属性 16GB 32GB
    private List<SaleVo> sales; // 打折满减信息等

}
