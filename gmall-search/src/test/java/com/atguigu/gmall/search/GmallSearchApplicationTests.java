package com.atguigu.gmall.search;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttr;
import com.atguigu.gmall.search.repositoty.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void data() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
    }
    @Test
    void dataAll() {

        Long pageNum = 1l;
        Long pageSize = 100l;

        do {
            // 分页查询spu

            QueryCondition queryCondition = new QueryCondition();
            queryCondition.setPage(pageNum);
            queryCondition.setLimit(pageSize);
            Resp<List<SpuInfoEntity>> spuResp = this.gmallPmsClient.querySpusByPage(queryCondition);
            List<SpuInfoEntity> spus = spuResp.getData();

            // 遍历spu，查询sku
            spus.forEach(spuInfoEntity -> {
                Resp<List<SkuInfoEntity>> skuResp = this.gmallPmsClient.querySkusBySpuId(spuInfoEntity.getId());
                List<SkuInfoEntity> skuInfoEntities = skuResp.getData();

                if (!CollectionUtils.isEmpty(skuInfoEntities)) {
                    // sku -> goods
                    List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                        Goods goods = new Goods();
                        // 查询搜索属性&值
                        Resp<List<ProductAttrValueEntity>> attrValueResp = this.gmallPmsClient.querySearchAttrValueBySpuId(spuInfoEntity.getId());
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


                        goods.setCreateTime(spuInfoEntity.getCreateTime());
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

            });

            pageSize = (long)spus.size();
            pageNum ++;

        } while (pageSize == 100);
    }
}
