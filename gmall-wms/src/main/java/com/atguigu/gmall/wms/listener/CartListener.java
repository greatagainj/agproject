package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.core.bean.Resp;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class CartListener {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "stock:lock:";
    
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "stock-unlock-queue", durable = "true"),
            exchange = @Exchange(value = "GMALL_ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"stock.unLock"}
    ))
    public void unlockListener(String orderToken) {

        String lockJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        if (StringUtils.isEmpty(lockJson)) {
            return;
        }
        List<SkuLockVo> lockVos = JSONObject.parseArray(lockJson, SkuLockVo.class);
        lockVos.forEach(skuLockVo -> {
            this.wareSkuDao.unLockStore(skuLockVo.getWareSkuId(), skuLockVo.getCount());

            // 删除redis中的库存锁定数据
            this.redisTemplate.delete(KEY_PREFIX + orderToken);
        });
    }

    // 减库存
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "stock-minus-queue", durable = "true"),
            exchange = @Exchange(value = "GMALL_ORDER_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void stockListener(String orderToken) {
        String lockJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        List<SkuLockVo> lockVos = JSONObject.parseArray(lockJson, SkuLockVo.class);
        lockVos.forEach(skuLockVo -> {
            this.wareSkuDao.minusStore(skuLockVo.getWareSkuId(), skuLockVo.getCount());
        });
    }
}
