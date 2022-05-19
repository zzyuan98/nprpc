package com.fixbug.callback;

/**
 * 描述：
 * @author zzyuan
 * @create 2022-04-06 22:13
 */
public interface INotifyProvider {
    /**
     * 回调操作 RpcServer 给 RpcProvider 上报收到的rpc服务调用相关参数信息
     * @param serverName
     * @param methodName
     * @param args
     * @return
     *
     */
    byte[] notify(String serverName , String methodName , byte[] args);
}
