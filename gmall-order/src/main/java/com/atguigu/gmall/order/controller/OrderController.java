package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.pay.AlipayTemplate;
import com.atguigu.gmall.order.pay.PayAsyncVo;
import com.atguigu.gmall.order.pay.PayVo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVo;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("confirm")
    public Resp<OrderConfirmVo> confirm() {

        OrderConfirmVo orderConfirmVo = this.orderService.confirm();

        return Resp.ok(orderConfirmVo);
    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVo submitVo){
        OrderEntity orderEntity = this.orderService.submit(submitVo);

        // TODO 由于内网穿透收费和调试麻烦，支付功能省略，如要恢复，可放开支付功能同时删除line：63代码
        /* 2020/04/07 支付功能 start*/
        /*PayVo payVo = new PayVo();
        try {
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setTotal_amount(orderEntity.getPayAmount() == null ? "0.1" : orderEntity.getPayAmount().toString());
            payVo.setSubject("gmall");
            payVo.setBody("支付平台");
            String form = this.alipayTemplate.pay(payVo);
            System.out.println(form);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }*/
        /* 2020/04/07 支付功能 end*/


        /* 2020/04/07 支付功能(假) start*/
        // 更改订单状态：待发货（本操作省略去扫码付款）
        this.amqpTemplate.convertAndSend("GMALL_ORDER_EXCHANGE", "order.pay", orderEntity.getOrderSn());
        /* 2020/04/07 支付功能（假） start*/

        return Resp.ok(null);
    }

    /* 2020/04/07 支付功能 start*/
    @PostMapping("pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo) {

        // 更改订单状态：待发货
        this.amqpTemplate.convertAndSend("GMALL_ORDER_EXCHANGE", "order.pay", payAsyncVo.getOut_trade_no());

        return Resp.ok(null);
    }

    /* 2020/04/07 支付功能 end*/
}
