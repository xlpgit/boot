package com.xlptest.boot.controller;

import com.xlptest.boot.consts.ErrorCode;
import com.xlptest.boot.entity.ResultPo;
import com.xlptest.boot.entity.UserInfo;
import com.xlptest.boot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("loginUI")
    public String loginUI() {
        return "login";
    }

    @RequestMapping("registerUI")
    public String registerUI() {
        return "register";
    }

    @RequestMapping("login")
    @ResponseBody
    public ResultPo login(String phone, String password) {
        ResultPo resultPo = new ResultPo();
        UserInfo userInfo = userService.getUser(phone);
        if (userInfo == null || !userInfo.getPassword().equals(password)) {
            resultPo.setCode(ErrorCode.USER_Not_FOUND);
            resultPo.setMessage("user not found");
            return resultPo;
        }
        resultPo.setCode(ErrorCode.SUCCESS);
        resultPo.setMessage("login success");
        resultPo.putData("user", userInfo);
        return resultPo;
    }

    @RequestMapping("register")
    @ResponseBody
    public ResultPo register(HttpServletRequest request, UserInfo userInfo) {
        return null;
    }
}
