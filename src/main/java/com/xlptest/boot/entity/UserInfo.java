package com.xlptest.boot.entity;


import com.alibaba.fastjson.annotation.JSONField;

public class UserInfo extends BaseRedisPo {

    private long id;
    private String phone;
    private String name;
    private int age;
    private String password;
    private String address;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public String getPrimary() {
        return this.phone;
    }

    @Override
    public String getKey() {
        return "h:user:list";
    }
}
