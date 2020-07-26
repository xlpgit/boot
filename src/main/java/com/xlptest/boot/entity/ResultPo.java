package com.xlptest.boot.entity;

import java.util.HashMap;
import java.util.Map;

//请求返回统一处理格式
public class ResultPo {
    private int code;
    private String message;
    private Map<Object, Object> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<Object, Object> getData() {
        return data;
    }

    public void setData(Map<Object, Object> data) {
        this.data = data;
    }

    public void putData(Object key, Object value) {
        if (data == null)
            data = new HashMap<>();
        data.put(key, value);
    }

    public static ResultPo getInstance() {
        return new ResultPo();
    }
}
