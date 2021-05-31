package com.xlptest.boot.serviceImp;

import com.xlptest.boot.service.HelloService;
import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String getName() {
        return "xlp";
    }

    @Override
    public String setName(String name) {
        return name;
    }
}
