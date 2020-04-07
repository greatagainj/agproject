package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.Interceptors.LoginInterceptor;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String KEY_PREFIX = "gmall:cart:";
    private static final String PRICE_PREFIX = "gmall:sku:";

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient SmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        
        String key = getLoginStatus();

        String skuId = cart.getSkuId().toString();
        // 放入购物车
        // 获取购物车，判断购物车是否有这件商品，有则更新数量，无则加入购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId)) {
            // 有，更新数量
            String cartJson = hashOps.get(skuId).toString();
            Cart product = JSONObject.parseObject(cartJson, Cart.class);
            product.setCount(product.getCount() + cart.getCount());
            hashOps.put(skuId, JSONObject.toJSONString(product));
        } else {
            // 无，加入购物车
            cart.setCheck(true);
            //通过skuId查询pms
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return;
            }
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());
            Resp<List<SkuSaleAttrValueEntity>> listResp = this.pmsClient.queryAttrValueBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> saleAttrValueEntityList = listResp.getData();
            cart.setSaleAttrValues(saleAttrValueEntityList);

            //通过skuId查询sms
            Resp<List<SaleVo>> listResp1 = this.SmsClient.querySalesBySkuId(cart.getSkuId());
            List<SaleVo> saleVoList = listResp1.getData();
            cart.setSales(saleVoList);

            //通过skuId查询wms
            Resp<List<WareSkuEntity>> listResp2 = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntityList = listResp2.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
                cart.setStore(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }

            // 保存当前价格
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuInfoEntity.getPrice().toString());

            // 购物车信息放入redis
            hashOps.put(skuId, JSONObject.toJSONString(cart));
        }
    }

    private String getLoginStatus() {
        // 获取登录信息
        String key = KEY_PREFIX;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getId() != null) {
            key += userInfo.getId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }

    public List<Cart> queryCarts() {

        // 获取登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String unLoginKey = KEY_PREFIX + userInfo.getUserKey();

        // 查询未登录购物车
        BoundHashOperations<String, Object, Object> unLoginHashOps = redisTemplate.boundHashOps(unLoginKey);
        List<Object> unLoginCartJsonList = unLoginHashOps.values();

        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(unLoginCartJsonList)) {
            unLoginCarts = unLoginCartJsonList.stream().map(unLoginCartJson -> {
                Cart cart = JSONObject.parseObject(unLoginCartJson.toString(), Cart.class);
                // 查询当前价格
                String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPrice));
                return cart;
            }).collect(Collectors.toList());
        }

        // 判断是否登录，未登录直接返回
        if (userInfo.getId() == null) {
            return unLoginCarts;
        }

        // 登录了，同步购物车，再查询
        String loginkey = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginkey);

        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    // 如果登录购物车包含未登录购物车，更新数量
                    Integer count = cart.getCount();
                    String hasCartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSONObject.parseObject(hasCartJson, Cart.class);
                    cart.setCount(cart.getCount() + count);
                }

                loginHashOps.put(cart.getSkuId().toString(), JSONObject.toJSONString(cart));
            });
            // 删除未登录购物车
            this.redisTemplate.delete(unLoginKey);
        }

        // 同步完毕，最终查询
        List<Object> loginCartJsonList = loginHashOps.values();
        return loginCartJsonList.stream().map(loginCartJson -> {
            Cart cart = JSONObject.parseObject(loginCartJson.toString(), Cart.class);
            // 查询当前价格
            String currentPrice = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
            cart.setCurrentPrice(new BigDecimal(currentPrice));
            return cart;
        }).collect(Collectors.toList());
    }

    public void updateCart(Cart cart) {
        String key = this.getLoginStatus();
        // 获取购物车
        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(key);
        Integer count = cart.getCount();
        // 判断请求修改的这条购物车信息
        if (boundHashOps.hasKey(cart.getSkuId().toString())) {
            String cartJson = boundHashOps.get(cart.getSkuId().toString()).toString();
            cart = JSONObject.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            boundHashOps.put(cart.getSkuId().toString(), JSONObject.toJSONString(cart));
        }
    }

    public void deleteCart(Long skuId) {
        String key = this.getLoginStatus();
        // 删除购物车
        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(key);
        if(boundHashOps.hasKey(skuId.toString())) {
            boundHashOps.delete(skuId.toString());
        }
    }

    public List<Cart> queryCartsByUserId(Long uerId) {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + uerId);
        List<Object> cartJsonList = hashOps.values();
        return cartJsonList.stream()
                .map(cartJson -> JSONObject.parseObject(cartJson.toString(), Cart.class))
                .filter(Cart::getCheck).collect(Collectors.toList());
    }
}
