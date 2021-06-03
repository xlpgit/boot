package com.xlptest.boot.controller;


import com.xlptest.boot.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("test")
public class HelloController {

    @Autowired
    HelloService helloService;

    @RequestMapping("hello")
    public String hello() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
        return "Hello jenkins,the current time is " + simpleDateFormat.format(date);
    }


}
