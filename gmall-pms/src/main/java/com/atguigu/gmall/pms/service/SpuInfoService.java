package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息
 *
 * @author greatagainj
 * @email @
 * @date 2020-03-13 19:10:01
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageVo queryPage(QueryCondition params);

    PageVo querySpuPage(QueryCondition queryCondition, Long cid);

    void bigSave(SpuInfoVo spuInfoVo);
}

