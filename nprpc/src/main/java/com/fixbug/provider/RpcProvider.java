package com.fixbug.provider;

import com.fixbug.callback.INotifyProvider;
import com.fixbug.util.ZKClientUtils;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Service;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * rpc发布的站点
 * 只需要一个站点就可以发布当前主机上所有的rpc方法了，用单例模式进行设计
 *
 * @author zzyuan
 * @create 2022-04-05 19:46
 */
public class RpcProvider implements INotifyProvider {

    private static final String SERVER_IP = "ip";
    private static final String SERVER_PORT = "port";
    private static final String ZK_SERVER = "zookeeper";
    private String serverIp;
    private int serverPort;
    private String zkServer;
    private ThreadLocal<byte[]> responsebufLocal;


    /**
     * 启动rpc站点提供服务
     *
     */
    public void start() {

        System.out.println("----------------start------------------");

        //todo..... 把server和method都往zookeeper上面注册一下ip地址和端口

        ZKClientUtils zk = new ZKClientUtils(zkServer);
        serviceMap.forEach((k,v) -> {

            String path = "/" + k;
            zk.createPersistent(path,null);
            //服务方法名称：临时性节点
            v.methodMap.forEach((a , b) -> {
                String createPath = path + "/" + a;
                zk.createEphemeral(createPath , serverIp + ":" + serverPort);
                //给临时性节点添加监听器watcher
                zk.addWatcher(createPath);
                System.out.println("reg zk -> " + createPath);
            });
        });

        System.out.println("rpc server start at :" + serverIp + " : " + serverPort);

        RpcServer s = new RpcServer(this);
        s.start(serverIp,serverPort);
    }

    /**
     * notify是多线程环境下被调用的
     *
     * 接收rpcServer网络模块上报的rpc调用的相关信息参数，执行具体的rpc方法调用
     * @param serverName
     * @param methodName
     * @param args
     * @return  把rpc方法调用完成以后的响应值进行返回
     */
    @Override
    public byte[] notify(String serverName, String methodName, byte[] args) {
        ServiceInfo si = serviceMap.get(serverName);
        Service service = si.service;//获取服务对象
        Descriptors.MethodDescriptor method = si.methodMap.get(methodName);//获取服务方法

        //从args序列化出method方法的参数的类型
        //LoginRequest RegRequest
        Message request = service.getRequestPrototype(method).toBuilder().build();

        try {
            request = request.getParserForType().parseFrom(args);//反序列化操作
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        /**
         * 调用rpc对象： server
         * rpc对象的方法：method
         * rpc方法的参数：request
         * 根据message.getName() => login
         *
         */

        service.callMethod(method,null , request ,
                message -> responsebufLocal.set(message.toByteArray()));

        return responsebufLocal.get();

    }

    /**
     * 服务方法的类型信息
     * 因为只有读操作，所有HashMap没有线程安全问题
     */
    private class ServiceInfo{
        Service service;
        Map<String, Descriptors.MethodDescriptor> methodMap;
        public ServiceInfo(){
            this.service = null;
            this.methodMap = new HashMap<>();
        }
    }

    //包含所有的服务对象和服务方法
    private Map<String , ServiceInfo> serviceMap;


    /**
     * 注册rpc服务方法
     * 只要支持rpc方法的类都实现了 com.google.protobuf.Service这个接口
     * @param service
     *
     */
    public void registerRpcService(Service service) {
        Descriptors.ServiceDescriptor sd = service.getDescriptorForType();
        //获取服务对象的名称
        String serviceName = sd.getName();
        //获取服务对象的所有服务方法列表
        List<Descriptors.MethodDescriptor> methodList = sd.getMethods();
        ServiceInfo si = new ServiceInfo();
        si.service = service;
        methodList.forEach(method -> {
            //获取服务方法名字
            String methodName = method.getName();
            si.methodMap.put(methodName,method);
        });
        serviceMap.put(serviceName,si);
    }

    /**
     * 封装RpcProvider对象的创建细节
     *
     */
    public static class Builder{
        private static RpcProvider INSTANCE = new RpcProvider();


        /**
         * 通过builder创建一个RpcProvider 对象
         * 从配置文件中读取rpc server的ip和port
         * 给instance对象初始化数据
         * @return
         */
        public RpcProvider build(String file){
            Properties pro = new Properties();
            try {
                pro.load(Builder.class.getClassLoader().getResourceAsStream(file));
                INSTANCE.setServerIp(pro.getProperty(SERVER_IP));
                INSTANCE.setServerPort(Integer.parseInt(pro.getProperty(SERVER_PORT)));
                INSTANCE.setZkServer(pro.getProperty(ZK_SERVER));
                return  INSTANCE;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    /**
     * 返回一个对象建造器
     * @return
     */
    public static Builder newBuilder(){
        return new Builder();
    }

    /**
     * 构造器
     */
    private RpcProvider(){
        this.serviceMap = new HashMap<>();
        this.responsebufLocal = new ThreadLocal<>();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
