package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CacheAspect {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        Object result = null;

        //获取目标方法的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        // 获取目标方法返回值
        Class<?> returnType = method.getReturnType();
        // 注解中的缓存前缀
        String prefix = gmallCache.prefix();
        // 获取目标方法的参数
        Object[] joinPointArgs = joinPoint.getArgs();

        String key = prefix + Arrays.asList(joinPointArgs).toString();

        // 从缓存中查询
        // 1、 命中，返回
        result = cacheHit(key, returnType);

        if (result != null) {
            return result;
        }

        // 2、没有命中，加分布式锁
        RLock lock = this.redissonClient.getLock("lock" + Arrays.asList(joinPointArgs).toString());
        lock.lock();


        // 3、再次查询，如果缓存中没有数据执行目标方法（目的是为了看自己加了锁之后这段事件别人有没有放这条数据）
        result = cacheHit(key, returnType);
        if (result != null) {
            lock.unlock();
            return result;
        }

        // 执行目标方法（DB检索）
         result = joinPoint.proceed(joinPointArgs);
        // 放入缓存
        this.redisTemplate.opsForValue().set(key,JSONObject.toJSONString(result),
                gmallCache.timeout() + (int) (Math.random() * gmallCache.random()), TimeUnit.MINUTES);
        lock.unlock();

        return result;
    }

    private Object cacheHit(String key, Class<?> returnType) {
        // 从缓存中查询

        String json = this.redisTemplate.opsForValue().get(key);

        // 1、 命中，返回
        if (StringUtils.isNotBlank(json)) {
            return JSONObject.parseObject(json, returnType);
        }
        return null;
    }

}
