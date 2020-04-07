package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

     // index:cates:8
    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLevel1Categories() {
        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoriesByPidOrLevel(1, null);
        return listResp.getData();
    }

    @GmallCache(prefix = "index:cates:", timeout = 7200, random = 100)
    public List<CategoryVo> queryLevel23Categories(Long pId) {
        // 判断缓存中有无23级分类数据，如果存在，就直接返回
//        String cateJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pId);
 //       if (!StringUtils.isEmpty(cateJson)) {
//            List<CategoryVo> categoryVos = JSONObject.parseArray(cateJson, CategoryVo.class);
//            return categoryVos;
//        }

        // redison分布式锁加锁
//        RLock lock = this.redissonClient.getLock("lock" + pId);
//        lock.lock();

        // 判断缓存中有无23级分类数据，如果存在，就直接返回
//        String cateJson2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pId);
//        if (!StringUtils.isEmpty(cateJson2)) {
//            List<CategoryVo> categoryVos2 = JSONObject.parseArray(cateJson2, CategoryVo.class);
//            lock.unlock();
//            return categoryVos2;
//        }

        // 如果没有，查询mysql
        Resp<List<CategoryVo>> categories = this.gmallPmsClient.subQueryCategories(pId);
        List<CategoryVo> categoryVos = categories.getData();
//        String jsonString = JSONObject.toJSONString(categoryVos);
        // 查询后放入缓存
//        this.redisTemplate.opsForValue().set(KEY_PREFIX + pId, jsonString, 30 + new Random().nextInt(5), TimeUnit.DAYS);

        // redison分布式锁解锁
//        lock.unlock();

        return categories.getData();
    }
}
