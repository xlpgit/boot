package com.xlptest.boot.test;public class Service2 {    public static void test() {        Services.Service servie = Services.getService("user");        String resutl = servie.hello();    }    public static void main(String[] args) {    }}