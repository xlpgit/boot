package com.xlptest.boot.serviceImp;

import com.xlptest.boot.entity.UserInfo;
import com.xlptest.boot.service.UserService;
import com.xlptest.boot.utils.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    RedisService redisService;
    String key = "h:user:list";

    @Override
    public UserInfo save(UserInfo userInfo) {
        redisService.hSetFromObject(userInfo);
        return userInfo;
    }

    @Override
    public UserInfo getUser(String phone) {
        return redisService.hGet(key, phone, UserInfo.class);
    }

    @Override
    public boolean updatePassword() {
        List<UserInfo> list = redisService.hgetFromObject("h:user:list", UserInfo.class);
        list.forEach(item-> {
            System.out.println(item.getPassword());
            item.setPassword("123456");
            redisService.hSetFromObject(item);
        });

        return false;
    }
}
