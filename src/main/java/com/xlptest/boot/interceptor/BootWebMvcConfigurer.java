package com.xlptest.boot.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BootWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new BootHandlerInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/user/loginUI")
                .excludePathPatterns("*.html");
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
