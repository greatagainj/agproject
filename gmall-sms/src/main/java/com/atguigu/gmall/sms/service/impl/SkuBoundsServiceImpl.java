package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuLadderDao skuLadderDao;

    @Autowired
    private SkuFullReductionDao skuFullReductionDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @Transactional
    public void saveSale(SkuSaleVo skuSaleVo) {
        // 保存营销相关三张表，需要feign被pms调用。
        // 1.sms_sku_bounds
        SkuBoundsEntity boundsEntity = new SkuBoundsEntity();
        //BeanUtils.copyProperties(skuSaleVo, boundsEntity);
        boundsEntity.setSkuId(skuSaleVo.getSkuId());
        boundsEntity.setGrowBounds(skuSaleVo.getGrowBounds());
        boundsEntity.setBuyBounds(skuSaleVo.getBuyBounds());
        List<Integer> work = skuSaleVo.getWork();
        boundsEntity.setWork(work.get(3) * 1 + work.get(2) * 2 + work.get(1) * 4 + work.get(0) * 8);
        this.save(boundsEntity);

        // 2.sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuSaleVo.getSkuId());
        skuLadderEntity.setFullCount(skuSaleVo.getFullCount());
        skuLadderEntity.setDiscount(skuSaleVo.getDiscount());
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        this.skuLadderDao.insert(skuLadderEntity);

        // 3.sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setSkuId(skuSaleVo.getSkuId());
        skuFullReductionEntity.setFullPrice(skuSaleVo.getFullPrice());
        skuFullReductionEntity.setReducePrice(skuSaleVo.getReducePrice());
        skuFullReductionEntity.setAddOther(skuSaleVo.getLadderAddOther());
        this.skuFullReductionDao.insert(skuFullReductionEntity);

    }

    @Override
    public List<SaleVo> querySalesBySkuId(Long skuId) {
        List<SaleVo> saleVos= new ArrayList<>();
        //查询积分信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            SaleVo boundsVo = new SaleVo();
            boundsVo.setType("积分");
            StringBuffer desc = new StringBuffer();
            if (skuBoundsEntity.getGrowBounds() != null && skuBoundsEntity.getGrowBounds() .intValue() > 0) {
                desc.append("成长积分送" + skuBoundsEntity.getGrowBounds()+ "点！ ");
            }
            if (skuBoundsEntity.getBuyBounds() != null && skuBoundsEntity.getBuyBounds().intValue() > 0) {
                desc.append("购物积分送" + skuBoundsEntity.getBuyBounds() + "点！");
            }
            boundsVo.setDesc(desc.toString());
            saleVos.add(boundsVo);
        }


        //查询打折信息
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuLadderEntity != null) {
            SaleVo ladderVo = new SaleVo();
            ladderVo.setType("打折");
            ladderVo.setDesc("本商品满" + skuLadderEntity.getFullCount() + "件，打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折！");
            saleVos.add(ladderVo);
        }


        //查询满减信息
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (skuFullReductionEntity != null) {
            SaleVo fullReductionVo = new SaleVo();
            fullReductionVo.setType("满减");
            fullReductionVo.setDesc("本商品满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice());
            saleVos.add(fullReductionVo);
        }
        return saleVos;
    }

}