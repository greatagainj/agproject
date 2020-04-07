package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.CategoryVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @PostMapping("pms/spuinfo/page")
    public Resp<List<SpuInfoEntity>> querySpusByPage(@RequestBody QueryCondition queryCondition);

    @GetMapping("pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>> querySkusBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    @GetMapping("pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("pms/productattrvalue/{spuId}")
    public Resp<List<ProductAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long supId);

    @GetMapping("pms/spuinfo/info/{id}")
    public Resp<SpuInfoEntity> querySpuById(@PathVariable("id") Long id);

    @GetMapping("pms/category")
    public Resp<List<CategoryEntity>> queryCategoriesByPidOrLevel(
            @RequestParam(value = "level", defaultValue = "0") Integer level,
            @RequestParam(value = "parentCid", required = false) Long pid);

    @GetMapping("pms/category/{pId}")
    public Resp<List<CategoryVo>> subQueryCategories(@PathVariable("pId")Long pid);

    @GetMapping("pms/skuinfo/info/{skuId}")
    public Resp<SkuInfoEntity> querySkuById(@PathVariable("skuId") Long skuId);

    @GetMapping("pms/skuimages/{skuId}")
    public Resp<List<SkuImagesEntity>> querySkuImagesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/skusaleattrvalue/{spuId}")
    public Resp<List<SkuSaleAttrValueEntity>> queryAttrValueEntities(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/spuinfodesc/info/{spuId}")
    public Resp<SpuInfoDescEntity> querySpuDesc(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    public Resp<List<ItemGroupVo>> queryItemGroupVoByCidAndSpuId(@PathVariable("cid")Long cid, @PathVariable("spuId")Long spuId);

    @GetMapping("pms/skusaleattrvalue/sku/{skuId}")
    public Resp<List<SkuSaleAttrValueEntity>> queryAttrValueBySkuId(@PathVariable("skuId") Long skuId);
}
