package com.xlptest.boot.controller;

import com.xlptest.boot.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;

@Controller
@RequestMapping("test")
public class HelloController {

    @Autowired
    HelloService helloService;

    @RequestMapping("/")
    public String hello() throws IOException {

        //获取resources下指定路径下的所有文件
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + "testdata/*.*");
        //保存文件路径
        for (Resource resource : resources) {
            //获取文件流
            InputStream inputStream = resource.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder json = new StringBuilder();
            String t;
            while ((t = reader.readLine()) != null) {
                json.append(t);
            }
            System.out.println(json);
            //获取文件名

            String filename = resource.getFilename();
            System.out.println(filename);
            //复制文件
            //File file = new File(transPath+filename);
            //对文件进行复制操作
            //FileUtils.copyInputStreamToFile(inputStream,file);
        }


        return "hello";
    }


}
