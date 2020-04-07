package com.atguigu.gmall.cart.Interceptors;

import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.core.bean.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;


    /**
     *
     * 统一获取登录状态
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        UserInfo userInfo = new UserInfo();

        // 获取cookie的token & userKey
        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());
        String userKey = CookieUtils.getCookieValue(request, this.jwtProperties.getUserKey());

        // 判断有没有userKey，没有的话制作一个放入cookie
        if (StringUtils.isEmpty(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, this.jwtProperties.getUserKey(), userKey, 6 * 30 *24 * 3600);
        }
        userInfo.setUserKey(userKey);

        // 判断有没有token
        if (StringUtils.isNotEmpty(token)) {
            // 解析token
            Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, this.jwtProperties.getPublicKey());
            userInfo.setId(new Long(infoFromToken.get("id").toString()));
        }

        THREAD_LOCAL.set(userInfo);

        return super.preHandle(request, response, handler);
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 必须手动清除线程变量
        THREAD_LOCAL.remove();
    }
}
