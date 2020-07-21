package com.xlptest.boot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xlptest.boot.entity.UserInfo;

public interface UserService {
    public UserInfo save(UserInfo userInfo);
    public UserInfo getUser(String phone);
}
