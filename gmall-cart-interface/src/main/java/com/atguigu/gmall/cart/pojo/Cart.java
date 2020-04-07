package com.atguigu.gmall.cart.pojo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Cart {

    private Long skuId;// 商品id
    private String title;// 标题
    private String defaultImage;// 图片
    private BigDecimal price;// 加入购物车时的价格
    private BigDecimal currentPrice;// 当前价格
    private Integer count;// 购买数量
    private Boolean store;
    private List<SkuSaleAttrValueEntity> saleAttrValues;// 销售属性 16GB 32GB
    private List<SaleVo> sales; // 打折满减信息等

    private Boolean check; // 勾选购物车
}
