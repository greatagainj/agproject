package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuInfoVo;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao infoDescDao;

    @Autowired
    private ProductAttrValueService attrValueService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuPage(QueryCondition queryCondition, Long cid) {

        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        // 判断分类是否为0，为0的话，查全站
        if (cid != null) {
            if (cid != 0) {
                wrapper.eq("catalog_id", cid);
            }
        }
        //判断关键字是否为空
        String key = queryCondition.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuInfoVo spuInfoVo) {
        // 保存spu相关三张表,6表顺序固定

        // 1.pms_spu_info
        Long spuId = saveSpuInfo(spuInfoVo);

        // 2.pms_spu_info_desc
        saveSpuInfoDesc(spuInfoVo, spuId);

        // 3.pms_product_attr_value
        saveBaseAttrValue(spuInfoVo, spuId);

        // 保存sku相关三张表,每个‘配置’的手机，都要保存相关配置以及营销信息
        saveSkuAndSale(spuInfoVo, spuId);

        //消息队列：生产者
        sendMsg("insert", spuId);
    }

    private void sendMsg(String type, Long spuId) {
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME, "item." + type, spuId);
    }

    private void saveSkuAndSale(SpuInfoVo spuInfoVo, Long spuId) {
        List<SkuInfoVo> skus = spuInfoVo.getSkus();
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(skuInfoVo -> {
            // 1.pms_sku_info
            skuInfoVo.setSpuId(spuId);
            skuInfoVo.setSkuCode(UUID.randomUUID().toString());
            skuInfoVo.setBrandId(spuInfoVo.getBrandId());
            skuInfoVo.setCatalogId(spuInfoVo.getCatalogId());
            List<String> skuInfoVoImages = skuInfoVo.getImages();
                // 设置默认图片
            if (!CollectionUtils.isEmpty(skuInfoVoImages)) {
                skuInfoVo.setSkuDefaultImg(StringUtils
                        .isNotBlank(skuInfoVo.getSkuDefaultImg()) ? skuInfoVo.getSkuDefaultImg() : skuInfoVoImages.get(0));
            }
            this.skuInfoDao.insert(skuInfoVo);
            Long skuId = skuInfoVo.getSkuId();

            // 2.pms_sku_images
            if (!CollectionUtils.isEmpty(skuInfoVoImages)) {
                List<SkuImagesEntity> skuImagesEntities = skuInfoVoImages.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    // 设置是否默认图片
                    skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoVo.getSkuDefaultImg(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.imagesService.saveBatch(skuImagesEntities);
            }

            // 3.pms_sku_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                // 设置skuId
                saleAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                // 批量保存销售属性
                this.saleAttrValueService.saveBatch(saleAttrs);
            }


            // 保存营销相关三张表，需要feign调用sms系统的接口。
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuInfoVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.gmallSmsClient.saveSale(skuSaleVo);

        });
    }

    private void saveBaseAttrValue(SpuInfoVo spuInfoVo, Long spuId) {
        List<BaseAttrVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> valueEntityList = baseAttrs.stream().map(baseAttrVo -> {
                ProductAttrValueEntity attrValueEntity = baseAttrVo;
                attrValueEntity.setSpuId(spuId);
                return attrValueEntity;
                    }).collect(Collectors.toList());

            this.attrValueService.saveBatch(valueEntityList);
        }
    }

    private void saveSpuInfoDesc(SpuInfoVo spuInfoVo, Long spuId) {
        List<String> images = spuInfoVo.getSpuImages();
        if (!CollectionUtils.isEmpty(images)) {
            SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
            descEntity.setSpuId(spuId);
            descEntity.setDecript(StringUtils.join(images, ","));
            this.infoDescDao.insert(descEntity);
        }
    }

    private Long saveSpuInfo(SpuInfoVo spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }

}