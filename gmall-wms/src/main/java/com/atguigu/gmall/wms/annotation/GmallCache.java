package com.atguigu.gmall.wms.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {

    /**
     * 缓存key前缀
     * @return
     */
    String prefix() default "";

    /**
     * 缓存过期世间分钟为单位
     * @return
     */
    int timeout() default 5;

    /**
     * 为防止雪崩，过期时间随机值范围
     * @return
     */
    int random() default 5;

}
