package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: UserServiceImpl
 *
 * @Author Mobai
 * @Create 2023/11/14 18:23
 * @Version 1.0
 * Description:
 */

@Service
public class UserServiceImpl implements UserService {

    private final String loginUrl = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;


    //发送Http请求请求微信服务器获取登录openId
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        //获取openid
        try {
            String openId = getOpenId(userLoginDTO.getCode());

            //如果没有获取到openId，就抛出登录异常
            if (openId == null) {
                throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
            }

            //判断用户是否存在
            User user = userMapper.getUser(openId);

            //创建Jwt的条件map
            Map<String, Object> map = new HashMap<>();

            //如果用户存在就直接返回登录成功
            if (user != null) {
                //生成Jwt令牌
                map.put(JwtClaimsConstant.USER_ID, user.getId());   //将用户id作为jwt令牌生成参数

            } else {
                //用户不存在就执行新增用户
                user = User.builder()
                        .openid(openId)
                        .createTime(LocalDateTime.now())
                        .build();

                userMapper.addUser(user);

                map.put(JwtClaimsConstant.USER_ID, user.getId());   //将用户id作为jwt令牌生成参数
            }
            //生成jwt令牌
            String jwt = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), map);

            //生成VO对象
            UserLoginVO userLoginVO = UserLoginVO.builder()
                    .id(user.getId())
                    .openid(user.getOpenid())
                    .token(jwt)
                    .build();

            //返回VO对象
            return userLoginVO;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String getOpenId(String loginCode) throws IOException {

        //封装Map(登录所需参数,appid,secret,js_code)
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", loginCode);
        map.put("grant_type", "authorization_code");

        String body = HttpClientUtil.doPost(loginUrl, map);
        JSONObject jsonObject = JSON.parseObject(body);
        String openid = (String) jsonObject.get("openid");

        return openid;
    }
}
