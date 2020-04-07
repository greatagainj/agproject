package com.atguigu.gmall.auth.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.core.utils.JwtUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.ums.entity.MemberEntity;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    // 封装的方法，利用@EnableConfigurationProperties(JwtProperties.class)来封装配置文件里的参数&封装公钥和私钥对象
    @Autowired
    private JwtProperties jwtProperties;

    public String accredit(String username, String password) {

        // 远程调用，校验用户名密码
        Resp<MemberEntity> memberEntityResp = this.umsClient.queryUser(username, password);
        MemberEntity memberEntity = memberEntityResp.getData();

        // 校验失败
        if (memberEntity == null) {
            return null;
        }

        // 校验成功，制作jwt： 载荷+私钥+过期时间

        try {
            // 载荷
            Map<String, Object> map = new HashMap<>();
            map.put("id", memberEntity.getId());
            map.put("username", memberEntity.getUsername());
            return JwtUtils.generateToken(map, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 放入cookie,在controller中做
        return null;
    }
}
