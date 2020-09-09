package com.xlptest.boot.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BootWebMvcConfigurer implements WebMvcConfigurer {
    private List<String> addaList = new ArrayList<>();
    private List<String> excludeList = new ArrayList<>();

    public void init(InterceptorRegistry registry) {
        addaList.add("/*/*");

        excludeList.add("/user/loginUI");
        excludeList.add("/user/registerUI");

        registry.addInterceptor(new BootHandlerInterceptor()).addPathPatterns(addaList)
                .excludePathPatterns(excludeList);


    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        this.init(registry);
        System.out.println("addInterceptors");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        System.out.println("addViewControllers");

//        registry.addViewController("").setViewName("");
    }
   /* @Override
    public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/index.html").setViewName("login");
           }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        registry.addInterceptor(new LoginHandlerInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login","/index.html","/tiger/login")
                .excludePathPatterns("/assets/**");

    }*/

}
