package com.fixbug.consumer;

import com.fixbug.RpcMetaProto;
import com.fixbug.provider.RpcProvider;
import com.fixbug.util.ZKClientUtils;
import com.google.protobuf.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * @author zzyuan
 * @create 2022-04-06 23:09
 */
public class RpcConsumer implements RpcChannel {

    private static final String ZK_SERVER = "zookeeper";

    private String zkServer;

    public RpcConsumer(String file) {
        Properties pro = new Properties();
        try {
            pro.load(RpcConsumer.class.getClassLoader().getResourceAsStream(file));
            this.zkServer = pro.getProperty(ZK_SERVER);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * stub代理对象需要接收一个RpcChannel的类的对象
     * 当用stub调用任意rpc方法的时候
     * 全部都调用了当前这个rpcChannel的callMethod()
     *
     * @param methodDescriptor
     * @param rpcController
     * @param message
     * @param message1
     * @param rpcCallback
     */
    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor,
                           RpcController rpcController,
                           Message message,//request
                           Message message1,//response
                           RpcCallback<Message> rpcCallback) {

        /**
         * 打包参数，递交网络发送
         * rpc调用的参数格式：header_size + service_name + method_name + args
         *
         */
        Descriptors.ServiceDescriptor sd = methodDescriptor.getService();
        String serviceName = sd.getName();
        String methodName = methodDescriptor.getName();

        //todo... 在zookeeper上查询serviceName-methodName在哪个主机上ip和port上
        String ip = "";
        int port = 0;
        ZKClientUtils zk = new ZKClientUtils(zkServer);
        String path = "/" + serviceName + "/" + methodName;
        String hostStr = zk.readData(path);
        zk.close();
        //读取不到信息
        if(hostStr == null){
            rpcController.setFailed("read path : " + path + " data from zookeeper is invalid!");
            rpcCallback.run(message1);
            return;
        }else{
            String[] host = hostStr.split(":");
            //读取ip地址和端口号
            ip = host[0];
            port = Integer.parseInt(host[1]);
        }

        //2.序列化头部信息
        RpcMetaProto.RpcMeta.Builder meta_builder = RpcMetaProto.RpcMeta.newBuilder();
        meta_builder.setServiceName(serviceName);
        meta_builder.setMethodName(methodName);
        byte[] metabuf = meta_builder.build().toByteArray();

        //参数
        byte[] argbuf = message.toByteArray();

        //组织rpc参数信息
        ByteBuf buf = Unpooled.buffer(4 + metabuf.length + argbuf.length);
        buf.writeInt(metabuf.length);
        buf.writeBytes(metabuf);
        buf.writeBytes(argbuf);

        //待发送的数据
        byte[] sendbuf = buf.array();

        //通过网络发送rpc调用请求信息
        Socket client = null;
        OutputStream out = null;
        InputStream in = null;


        try {
            client = new Socket();
            client.connect(new InetSocketAddress(ip , port));
            out = client.getOutputStream();
            in = client.getInputStream();

            //发送数据
            out.write(sendbuf);
            out.flush();

            //wait等待rpc调用的响应返回
            ByteArrayOutputStream recvbuf = new ByteArrayOutputStream();
            byte[] rbuf = new byte[1024];
            int size = in.read(rbuf);

            /**
             * size 可能是 0
             * 因为RpcProvider封装Response响应参数的时候，如果响应参数的成员变量的参数的值都是默认值
             * 实际上RpcProvider给RpcServer就是一个空数据
             *
             */
            if(size > 0){
                recvbuf.write(rbuf,0,size);
                rpcCallback.run(message1.getParserForType().parseFrom(recvbuf.toByteArray()));
            }else {
                rpcCallback.run(message1);
            }

        } catch (IOException e) {
            rpcController.setFailed("server connect error , check server !");
            rpcCallback.run(message1);
        }finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(client != null){
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
