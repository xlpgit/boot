package com.xlptest.boot.controller;

import com.xlptest.boot.entity.UserInfo;
import com.xlptest.boot.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;

@Controller
@RequestMapping("test")
public class HelloController {

    @Autowired
    HelloService helloService;

    @RequestMapping("hello")
    public String hello(Model model) {
        model.addAttribute("now", DateFormat.getDateTimeInstance());
        return "hello";
    }



}
