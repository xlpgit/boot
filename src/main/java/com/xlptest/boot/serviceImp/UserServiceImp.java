package com.xlptest.boot.serviceImp;

import com.xlptest.boot.entity.UserInfo;
import com.xlptest.boot.service.UserService;
import com.xlptest.boot.utils.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    RedisService redisService;
    String key = "h:user:list";

    @Override
    public UserInfo save(UserInfo userInfo) {
        redisService.hSet(key, userInfo.getPhone(), userInfo);
        return userInfo;
    }

    @Override
    public UserInfo getUser(String phone) {
        return redisService.hGet(key, phone, UserInfo.class);
    }
}
