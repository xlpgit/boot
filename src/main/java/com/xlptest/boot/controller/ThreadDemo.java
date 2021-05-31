package com.xlptest.boot.controller;

/**
 * @Autor xuleping
 * @Description
 * @date 2021/3/5
 */
public class ThreadDemo{
    public static void main(String[] args) {
        myThread m1=new myThread("线程A");
        myThread m2=new myThread("线程B");
        m1.start();
        m2.start();
    }
}
class myThread extends Thread{
    private String name;
    public myThread(String name){
        this.name=name;
    }

    @Override
    public void run() {
        for(int i=0;i<10;i++){
            System.out.println(name+"运行，i="+i);
        }
    }
}
