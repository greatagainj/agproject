package com.atguigu.gmall.cart.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CartListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    private static final String PRICE_PREFIX = "gmall:sku:";
    private static final String CART_PREFIX = "gmall:cart:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart-item-queue", durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.update"}
    ))
    public void listener(Long spuId) {
        Resp<List<SkuInfoEntity>> skuListResp = this.pmsClient.querySkusBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuListResp.getData();
        skuInfoEntities.forEach(skuInfoEntity -> {
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuInfoEntity.getSkuId().toString(), skuInfoEntity.getPrice().toString());
        });
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart-delete-queue", durable = "true"),
            exchange = @Exchange(value = "GMALL_ORDER_EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"cart.delete"}
    ))
    public void deleteListener(Map<String, Object> map) {

        Long userId = (Long) map.get("userId");
        List<Object> skuIds = (List<Object>) map.get("skuIds");

        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(CART_PREFIX + userId.toString());
        List<String> skus = skuIds.stream().map(skuId -> skuId.toString()).collect(Collectors.toList());
        String[] ids = skus.toArray(new String[skus.size()]);

        boundHashOps.delete(ids);
    }
}
