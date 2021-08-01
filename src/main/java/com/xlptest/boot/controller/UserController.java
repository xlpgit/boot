package com.xlptest.boot.controller;

import com.xlptest.boot.consts.ErrorCode;
import com.xlptest.boot.entity.ResultPo;
import com.xlptest.boot.entity.UserInfo;
import com.xlptest.boot.service.UserService;
import com.xlptest.boot.utils.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
@RequestMapping("user")
public class UserController  extends BaseController{

    @Autowired
    UserService userService;
    @Autowired
    RedisService redisService;

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
    public ResultPo login(HttpServletRequest request, String phone, String password) {
        ResultPo resultPo = new ResultPo();
        UserInfo userInfo = userService.getUser(phone);
        if (userInfo == null || !userInfo.getPassword().equals(password)) {
            resultPo.setCode(ErrorCode.USER_Not_FOUND);
            resultPo.setMessage("user not found");
            return resultPo;
        }
        //获取session和token，并保存到redis中,30分钟失效
        //若session已经存在，且在有效期内，无法在另一处登陆，现在加上顶替
        HttpSession session = request.getSession();
        String token = session.getId();
        String key = "token:" + userInfo.getId();
        String oldSession = redisService.getString(key);
        if (session.equals(oldSession)) {
            resultPo.setCode(ErrorCode.SUCCESS);
            resultPo.setMessage("login success");
        } else {
            try {
                redisService.setString(key, token, 1800);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resultPo.setCode(ErrorCode.SUCCESS);
        resultPo.setMessage("login success");
        resultPo.putData("user", userInfo);
        return resultPo;
    }

    @RequestMapping("register")
    @ResponseBody
    public ResultPo register(HttpServletRequest request, UserInfo userInfo) {
        Date date = new Date();
        long time = date.getTime();
        userInfo.setId(time);
        userService.save(userInfo);//前台传过来的
        ResultPo resultPo = ResultPo.getInstance();
        resultPo.setCode(ErrorCode.SUCCESS);
        resultPo.setMessage("register");
        resultPo.putData("user", userInfo);
        return resultPo;
    }

    @RequestMapping("test")
    @ResponseBody
    public String test() {
        userService.updatePassword();
        return "success";
    }
}
