package com.fixbug;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原来是本地 method()
 * 现在要发布成一个RPC method()
 *
 * @author zzyuan
 * @create 2022-04-05 17:14
 */
public class UserServiceImpl extends UserServiceProto.UserServiceRpc {

    private ConcurrentHashMap<String,String> userMap = new ConcurrentHashMap();

    /**
     * 登录业务
     * @param name
     * @param pwd
     * @return
     */
    public boolean login(String name , String pwd){
        System.out.println("call UserServiceImpl -> login");
        System.out.println("name : " + name);
        System.out.println("pwd : " + pwd);

        if(userMap.containsKey(name)){
            return true;
        }else {
            return false;
        }

    }


    /**
     * 注册业务
     * @param name
     * @param pwd
     * @param age
     * @param sex
     * @param phone
     * @return
     */
    public boolean reg(String name , String pwd ,int age , String sex , String phone){
        System.out.println("call UserServiceImpl -> reg");
        System.out.println("name : " + name);
        System.out.println("pwd : " + pwd);
        System.out.println("age : " + age);
        System.out.println("sex : " + sex);
        System.out.println("phone : " + phone);
        userMap.put(name,pwd);
        return true;
    }

    /**
     * login的rpc代理方法
     * @param controller    可以接收方法的执行状态
     * @param request
     * @param done
     */
    @Override
    public void login(RpcController controller,
                      UserServiceProto.LoginRequest request,
                      RpcCallback<UserServiceProto.Response> done) {
        //1.从request里面读取到远程rpc调用的参数
        String name = request.getName();
        String pwd = request.getPwd();

        //2.根据解析的参数做本地业务
        boolean result = login(name,pwd);

        //3.填写方法的响应值
        UserServiceProto.Response.Builder response_builder = UserServiceProto.Response.newBuilder();
        response_builder.setErrno(0);
        response_builder.setErrinfo("");
        response_builder.setResult(result);


        //4.把response对象给到nprpc框架，由框架负责发送rpc调用的响应值
        done.run(response_builder.build());
    }

    /**
     * reg的rpc代理方法
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void reg(RpcController controller,
                    UserServiceProto.RegRequest request,
                    RpcCallback<UserServiceProto.Response> done) {
        String name = request.getName();
        String pwd = request.getPwd();
        int age = request.getAge();
        String phone = request.getPhone();

        boolean result = reg(name, pwd, age, "MAN", phone);

        UserServiceProto.Response.Builder response_builder = UserServiceProto.Response.newBuilder();
        response_builder.setErrno(0);
        response_builder.setErrinfo("");
        response_builder.setResult(result);
        //4.把response对象给到nprpc框架，由框架负责发送rpc调用的响应值
        done.run(response_builder.build());
    }
}
