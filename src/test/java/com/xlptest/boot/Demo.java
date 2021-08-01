package com.xlptest.boot;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.*;

/**
 * TestNG常用的注解
 */
public class Demo {


    // 在该套件的所有测试都运行在注释的方法之前，仅运行一次
    @BeforeSuite
    public void beforeSuite() {
        System.out.println("beforeSuite");
    }

    // 注释的方法将在属于test标签内的类的所有测试方法运行之前运行
    @BeforeTest
    public void beforeTest() {
        System.out.println("beforeTest");
    }

    // 在调用当前类的第一个测试方法之前运行，注释方法仅运行一次
    @BeforeClass
    public void beforeClass() {
        System.out.println("beforeClass");
    }

    //注释方法将在每个测试方法之前运行
    @BeforeMethod
    public void beforeMethod() {
        System.out.println("beforeMethod");
    }

    //将类或方法标记为测试的一部分，此标记若放在类上，则该类所有公共方法都将被作为测试方法
    @Test
    public void demo() {
        System.out.println("this is a demo");
        //testNG断言
        Assert.assertTrue(true);
    }

    //注释方法将在每个测试方法之后运行
    @AfterMethod
    public void afterMethod() {
        System.out.println("afterMethod");
    }

    //配置方法将在之前运行组列表。 此方法保证在调用属于这些组中的任何一个的第一个测试方法之前不久运行
    @BeforeGroups(groups = "group1")
    public void beforeGroups() {
        System.out.println("beforeGroups");
    }

    //beforeMethod

    @Test(groups = "group1")
    public void demo1() {
        System.out.println("demo1 from group1");
        Assert.assertTrue(true);
    }

    //afterMethod

    //此配置方法将在之后运行组列表。该方法保证在调用属于任何这些组的最后一个测试方法之后不久运行
    @AfterGroups(groups = "group1")
    public void afterGroups() {
        System.out.println("afterGroups");
    }

    //beforeMethod

    @Test(groups = "group2")
    public void demo2() {
        System.out.println("demo2 from group2");
        Assert.assertTrue(true);
    }

    //afterMethod

    //在调用当前类的第一个测试方法之后运行，注释方法仅运行一次
    @AfterClass
    public void afterClass() {
        System.out.println("afterClass");
    }

    // @afterTest 注释的方法将在属于test标签内的类的所有测试方法运行之后运行
    @AfterTest
    public void afterTest() {
        System.out.println("afterTest");
    }


    //@AfterSuite 在该套件的所有测试都运行在注释方法之后，仅运行一次
    @AfterSuite
    public void afterSuite() {
        System.out.println("afterSuite");
    }


}


/**
 * result: beforeSuite、beforeTest、beforeClass、beforeMethod、this is a demo、afterMethod、beforeGroups、beforeMethod、
 * demo1 from group1、afterMethod、afterGroups、beforeMethod、demo2 from group2、afterMethod、afterClass、afterTest、
 * afterSuite
 */