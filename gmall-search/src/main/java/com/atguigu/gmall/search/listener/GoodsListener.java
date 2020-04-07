package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttr;
import com.atguigu.gmall.search.repositoty.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {


    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gmall-search-queue", durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.insert", "item.update"}
    ))
    public void listener(Long spuId) {

        // 查询出spu sku ，放入es
        Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkusBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuResp.getData();

        if (!CollectionUtils.isEmpty(skuInfoEntities)) {
            // sku -> goods
            List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();
                // 查询搜索属性&值
                Resp<List<ProductAttrValueEntity>> attrValueResp = this.gmallPmsClient.querySearchAttrValueBySpuId(spuId);
                List<ProductAttrValueEntity> valueRespData = attrValueResp.getData();
                if (!CollectionUtils.isEmpty(valueRespData)) {
                    List<SearchAttr> searchAttrs = valueRespData.stream().map(productAttrValueEntity -> {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttr;
                    }).collect(Collectors.toList());
                    goods.setAttrs(searchAttrs);
                }

                // 查询品牌
                Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goods.setBrandId(skuInfoEntity.getBrandId());
                    goods.setBrandName(brandEntity.getName());
                }

                // 查询分类
                Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntity != null) {
                    goods.setCategoryId(skuInfoEntity.getCatalogId());
                    goods.setCategoryName(categoryEntity.getName());
                }

                Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(spuId);
                goods.setCreateTime(spuInfoEntityResp.getData().getCreateTime());
                goods.setPic(skuInfoEntity.getSkuDefaultImg());
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());
                goods.setSale(0l);
                goods.setSkuId(skuInfoEntity.getSkuId());
                //查询库存
                Resp<List<WareSkuEntity>> saleResp = this.gmallWmsClient.queryWareSkuBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = saleResp.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);
                    goods.setStore(flag);
                }

                goods.setTitle(skuInfoEntity.getSkuTitle());
                return goods;
            }).collect(Collectors.toList());

            // 导入index
            this.goodsRepository.saveAll(goodsList);
        }
    }
    //@RabbitListener(bindings = @QueueBinding( ...... 可以多个监听
}
