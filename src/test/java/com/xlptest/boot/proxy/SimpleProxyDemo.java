package com.xlptest.boot.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Autor xuleping
 * @Description
 * @date 2021/3/25
 */
class RealObject implements Interface {

    @Override
    public void something() {
        System.out.println("doSomething");
    }

    @Override
    public void somethingElse(String args) {
        System.out.println("somethingElse " + args);
    }
}

class SimpleProxy implements Interface {

    private Interface proxied;

    public SimpleProxy(Interface proxied) {
        this.proxied = proxied;
    }

    @Override
    public void something() {
        System.out.println("SimpleProxy doSomething");
        proxied.something();
    }

    @Override
    public void somethingElse(String args) {
        System.out.println("SimpleProxy somethingElse " + args);
        proxied.somethingElse(args);
    }
}

class DynamicProxyHandler implements InvocationHandler {

    private Object proxied;

    public DynamicProxyHandler(Object proxied) {
        this.proxied = proxied;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("**** proxy:"+proxy.getClass()+",method:"+method+",args"+args);
        return method.invoke(proxied,args);
    }
}

public class SimpleProxyDemo {
    public static void consumer(Interface iface) {
        iface.something();
        iface.somethingElse("bobo");
    }

    public static void main(String[] args) {
        /*consumer(new RealObject());
        consumer(new SimpleProxy(new RealObject()));*/
        RealObject real=new RealObject();
        consumer(real);
        Interface proxy= (Interface) Proxy.newProxyInstance(Interface.class.getClassLoader(),new Class[]{Interface.class},new DynamicProxyHandler(real));
        consumer(proxy);
    }
}
