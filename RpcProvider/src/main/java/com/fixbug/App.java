package com.fixbug;

import com.fixbug.provider.RpcProvider;

import javax.xml.ws.spi.Provider;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        /**
         * 启动一个可以启动rpc远程方法调用的Server
         * 1.需要一个RpcProvider（rpc提供的对象）
         * 2.向RpcProvider上面注册rpc方法，UserServiceImpl.login UserServiceImpl.Reg
         * 3.启动rpcProvider这个Server站点了 阻塞等待远程rpc方法调用请求
         *
         */
        RpcProvider.Builder builder = RpcProvider.newBuilder();
        RpcProvider provider = builder.build("config.properties");

        /**
         * UserServiceImpl：服务对象的名称
         * Login , reg : 服务方法的名称
         */
        provider.registerRpcService(new UserServiceImpl());

        /**
         * 启动rpc server 站点了，阻塞等待远程rpc调用的请求
         */
        provider.start();

    }
}
