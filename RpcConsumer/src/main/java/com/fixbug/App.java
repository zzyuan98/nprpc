package com.fixbug;

import com.fixbug.consumer.RpcConsumer;
import com.fixbug.controller.NrpcController;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        /**
         * 模拟rpc方法调用者
         *  google grpc
         *
         */
        UserServiceProto.UserServiceRpc.Stub stub =
                UserServiceProto.UserServiceRpc
                        .newStub(new RpcConsumer("config.properties"));

        UserServiceProto.LoginRequest.Builder login_builder = UserServiceProto.LoginRequest.newBuilder();
        login_builder.setName("zhangsan");
        login_builder.setPwd("88888888");

        NrpcController con = new NrpcController();

        stub.login(con, login_builder.build() , response ->{
            if(con.failed()){//rpc方法没有调用成功
                System.out.println(con.errorText());
            }else {
                System.out.println("receive rpc call response !");
                if(response.getErrno() == 0){//调用正常
                    System.out.println(response.getResult());
                }else{//调用出错
                    System.out.println(response.getErrinfo());
                }
            }
        });

        UserServiceProto.RegRequest.Builder reg_builder = UserServiceProto.RegRequest.newBuilder();

        reg_builder.setName("zhangsan");
        reg_builder.setPwd("88888888");
        reg_builder.setAge(24);
        reg_builder.setPhone("182991");



        stub.reg(con, reg_builder.build() , response ->{
            if(con.failed()){//rpc方法没有调用成功
                System.out.println(con.errorText());
            }else {
                System.out.println("receive rpc call response !");
                if(response.getErrno() == 0){//调用正常
                    System.out.println(response.getResult());
                }else{//调用出错
                    System.out.println(response.getErrinfo());
                }
            }
        });


    }
}
