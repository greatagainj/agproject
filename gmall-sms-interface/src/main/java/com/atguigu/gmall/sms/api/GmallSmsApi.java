package com.atguigu.gmall.sms.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sku/sale/save")
    public Resp<Object> saveSale(@RequestBody SkuSaleVo skuSaleVo);

    @GetMapping("sms/skubounds/{skuId}")
    public Resp<List<SaleVo>> querySalesBySkuId(@PathVariable("skuId") Long skuId);
}
