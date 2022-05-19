package com.fixbug;

import static org.junit.Assert.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * 测试序列化和反序列化
     */
    @Test
    public void test1(){

        TestProto.LoginRequest.Builder login_builder = TestProto.LoginRequest.newBuilder();

        login_builder.setName("张三");
        login_builder.setPwd("123456");

        TestProto.LoginRequest request = login_builder.build();
        System.out.println(request.getName());
        System.out.println(request.getPwd());
        /**
         * 把LoginRequest对象序列化成字节流，通过网络发送出去
         * 此处的sendbuf通过网络发送出去了
         */
        byte[] sendbuf = request.toByteArray();
        /**
         * Protobuf 从byte[] 数组反序列化成LoginRequest
         */
        try {
            TestProto.LoginRequest r = TestProto.LoginRequest.parseFrom(sendbuf);
            System.out.println(r.getName());
            System.out.println(r.getPwd());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

//    message RegRequest {
//    string name = 1;  //String
//    string pwd = 2;   //String
//    int32 age = 3;  //int
//    enum SEX {
//        MAN = 0;
//        WOMAN = 1;
//    }
//    SEX sex = 4;
//    string phone = 5;
//}
//
    @Test
    public void test2(){
        TestProto.RegRequest.Builder reg_builder = TestProto.RegRequest.newBuilder();
        reg_builder.setName("zzyuan");
        reg_builder.setPwd("123456");
        reg_builder.setAge(24);
        reg_builder.setSex(TestProto.RegRequest.SEX.MAN);
        reg_builder.setPhone("1281810011");
        TestProto.RegRequest regRequest = reg_builder.build();
        System.out.println(regRequest.getName());
        System.out.println(regRequest.getPwd());
        System.out.println(regRequest.getAge());
        System.out.println(regRequest.getSex());
        System.out.println(regRequest.getPhone());
        byte[] sendbuf = regRequest.toByteArray();

        System.out.println("-------------------------------");

        try {
            TestProto.RegRequest r = TestProto.RegRequest.parseFrom(sendbuf);
            System.out.println(r.getName());
            System.out.println(r.getPwd());
            System.out.println(r.getAge());
            System.out.println(r.getSex());
            System.out.println(r.getPhone());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3(){
        Properties pro = new Properties();
        try {
            pro.load(AppTest.class.getClassLoader().getResourceAsStream("config.properties"));
            System.out.println(pro.getProperty("IP"));
            System.out.println(pro.getProperty("PORT"));
            System.out.println(pro.getProperty("ZOOKEEPER"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
