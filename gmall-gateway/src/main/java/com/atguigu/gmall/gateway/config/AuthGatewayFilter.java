package com.atguigu.gmall.gateway.config;


import com.atguigu.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class AuthGatewayFilter implements GatewayFilter {

    @Autowired
    JwtProperties jwtProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 0、获取请求响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //1、获取jwt类型token信息
        //1.1、获取cookie
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        if (CollectionUtils.isEmpty(cookies)) {
            // 拦截 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        HttpCookie cookie = cookies.getFirst(this.jwtProperties.getCookieName());

        //2、判断jwt类型token信息是非为空
        if (cookie == null) {
            // 拦截 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //3、解析jwt类型token信息，如果正常解析放行，异常拦截
        try {
            JwtUtils.getInfoFromToken(cookie.getValue(), this.jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            // 拦截 401
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 放行
        return chain.filter(exchange);
    }
}
