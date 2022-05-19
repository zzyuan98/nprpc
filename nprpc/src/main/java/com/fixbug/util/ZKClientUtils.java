package com.fixbug.util;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 和zookeeper通信用的辅助工具类
 *
 * @author zzyuan
 * @create 2022-04-07 0:44
 */
public class ZKClientUtils {

    private static String rootPath = "/nprpc";

    private ZkClient zkClient;
    //临时性节点map
    private Map<String,String> ephemeralMap = new HashMap<>();

    /**
     * 通过zk server字符串信息连接zkServer
     * @param serverList
     */
    public ZKClientUtils(String serverList) {
        this.zkClient = new ZkClient(serverList,3000);
        //如果root节点不存在创建root节点
        if(!this.zkClient.exists(rootPath)){
            this.zkClient.createPersistent(rootPath,null);
        }
    }

    /**
     * 关闭和zkServer的连接
     */
    public void close(){
        this.zkClient.close();
    }

    /**
     * zk上创建临时性节点
     * @param path
     * @param data
     */
    public void createEphemeral(String path , String data){
        path = rootPath + path;
        ephemeralMap.put(path,data);
        //不存在才创建
        if(!this.zkClient.exists(path)){
            this.zkClient.createEphemeral(path,data);

        }
    }

    /**
     * zk上创建永久性节点
     * @param path
     * @param data
     */
    public void createPersistent(String path , String data){
        path = rootPath + path;
        ephemeralMap.put(path,data);
        //不存在才创建
        if(!this.zkClient.exists(path)){
            this.zkClient.createPersistent(path,data);
        }
    }

    /**
     * 读取znode节点的值
     * @param path
     * @return
     */
    public String readData(String path){
        return this.zkClient.readData(rootPath+path,null);
    }


    /**
     * 给 zk上面指定的znode添加watcher监听
     * @param path
     */
    public void addWatcher(String path){
        this.zkClient.subscribeDataChanges(rootPath + path, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            /**
             * 一定要设置znode节点监听，因为如果zkclient断掉，由于zk-server 无法直接获知zkclinet的关闭状态
             * 所有zkserver会等待session timeout时间（30S）以后，会把zkclient创建的临时节点全部删除掉
             * 但是如果在session timeout时间之内，又启动了同样的zkclient，那么等待session timeout超时以后
             * 原先创建的临时节点都没有了
             *
             * @param path
             * @throws Exception
             */
            @Override
            public void handleDataDeleted(String path) throws Exception {
                System.out.println("watcher -> handleDataDeleted : " + path);
                //把删除掉的临时性节点重新创建一下
                String str = ephemeralMap.get(path);
                if(str != null) {
                    //直接创建节点
                    zkClient.createEphemeral(path, str);
                }

            }

        });

    }

    public static void main(String[] args) {
        ZKClientUtils zk = new ZKClientUtils("127.0.0.1:2181");
        zk.createPersistent("/ProductService","123456");
        System.out.println(zk.readData("/ProductService"));
        zk.close();
    }

    public static String getRootPath() {
        return rootPath;
    }

    public static void setZkClient(String rootPath) {
        ZKClientUtils.rootPath = rootPath;
    }

}
