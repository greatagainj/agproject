package com.atguigu.gmall.item.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    public ItemVo queryItemVo(Long skuId) {

        ItemVo itemVo = new ItemVo();
        itemVo.setSkuId(skuId);
        // 根据skuId查询sku数据
          // 创建一个线程 1
        CompletableFuture<Object> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuResp.getData();
            if (skuInfoEntity == null) {
                return itemVo;
            }
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVo.setSkuSubtitle(skuInfoEntity.getSkuSubtitle());
            itemVo.setPrice(skuInfoEntity.getPrice());
            itemVo.setWeight(skuInfoEntity.getWeight());

            Long spuId = skuInfoEntity.getSpuId();
            itemVo.setSpuId(spuId);

            return skuInfoEntity;
        }, threadPoolExecutor);

        // 这个线程依赖于上面的线程 1-1
        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中的spuid查询spu信息
            Resp<SpuInfoEntity> spuResp = this.gmallPmsClient.querySpuById(((SkuInfoEntity) skuInfoEntity).getSpuId());
            SpuInfoEntity spuInfoEntity = spuResp.getData();
            if (spuInfoEntity != null) {
                itemVo.setSpuName(spuInfoEntity.getSpuName());
            }
        }, threadPoolExecutor);

        // 线程2
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            // 根据skuId查询sku图片列表
            Resp<List<SkuImagesEntity>> images = this.gmallPmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> imagesData = images.getData();
            itemVo.setPics(imagesData);
        }, threadPoolExecutor);


        // 线程1-2  依赖于sku数据（线程1）
        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中的brandId和categoryId查询品牌
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(((SkuInfoEntity) skuInfoEntity).getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            itemVo.setBrandEntity(brandEntity);
            }, threadPoolExecutor);

        // 线程1-3  依赖于sku数据（线程1）
        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据sku中的brandId和categoryId查询分类
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(((SkuInfoEntity) skuInfoEntity).getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            itemVo.setCategoryEntity(categoryEntity);
            }, threadPoolExecutor);


        // 线程3
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            // 根据skuId查询营销信息
            Resp<List<SaleVo>> querySalesBySkuId = this.gmallSmsClient.querySalesBySkuId(skuId);
            List<SaleVo> salesBySkuIdData = querySalesBySkuId.getData();
            itemVo.setSales(salesBySkuIdData);
        }, threadPoolExecutor);


        // 线程4
        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            // 根据skuId查询库存信息
            Resp<List<WareSkuEntity>> wareSkuBySkuId = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuBySkuIdData = wareSkuBySkuId.getData();
            itemVo.setStore(wareSkuBySkuIdData.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }, threadPoolExecutor);


        //线程1-4 依赖于线程1
        CompletableFuture<Void> saleAttrCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据spuId查询所有skuId，再查询所有sku的销售属性
            Resp<List<SkuSaleAttrValueEntity>> salesAttrResp = this.gmallPmsClient.queryAttrValueEntities(((SkuInfoEntity) skuInfoEntity).getSpuId());
            List<SkuSaleAttrValueEntity> saleAttrValueEntities = salesAttrResp.getData();
            itemVo.setSaleAttrs(saleAttrValueEntities);
        }, threadPoolExecutor);

        //线程1-5 依赖于线程1
        CompletableFuture<Void> descCompletableFuture =  skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据spuId查询海报
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDesc(((SkuInfoEntity) skuInfoEntity).getSpuId());
            SpuInfoDescEntity descEntity = spuInfoDescEntityResp.getData();
            if (descEntity != null) {
                String decript = descEntity.getDecript();
                String[] desc = StringUtils.split(decript, ",");
                itemVo.setImages(Arrays.asList(desc));
            }
        }, threadPoolExecutor);

        //线程1-6 依赖于线程1
        CompletableFuture<Void> attrsCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 根据分类id和spuId查询组和组下的规格参数 带值
            Resp<List<ItemGroupVo>> attrValueResp = this.gmallPmsClient.queryItemGroupVoByCidAndSpuId(((SkuInfoEntity) skuInfoEntity).getCatalogId(), ((SkuInfoEntity) skuInfoEntity).getSpuId());
            List<ItemGroupVo> itemGroupVos = attrValueResp.getData();
            itemVo.setAttrGroups(itemGroupVos);
        }, threadPoolExecutor);

        // 阻塞线程，必须全部完成才能返回itemVo
        CompletableFuture.allOf(spuCompletableFuture, imageCompletableFuture, brandCompletableFuture, categoryCompletableFuture,
                salesCompletableFuture, storeCompletableFuture, saleAttrCompletableFuture, descCompletableFuture, attrsCompletableFuture).join();

        return itemVo;
    }
}
