# nprpc
a rpc framework base on Protobuf , Zookeeper , Netty  developed by Java

项目流程图：

![image](https://user-images.githubusercontent.com/75787932/169328079-85631171-8bb6-4632-84f4-bc2d16e3d6ba.png)

如何启动：



- ```txt
  - 安装好zookeeper，启动zookeeper
  - zookeeper的配置信息在config.properties文件中
  - 启动RpcProvider项目下的 com.fixbug.provider.RpcProvider.App类下面的main方法
  - 启动RpcConsumer项目下的 com.fixbug.consumer.RpcConsumer.App类下面的main方法
  ```



功能：实现了RpcConsumer通过rpc，对RpcProvider方 发起，调用实现简单的CRUD（登录注册等等）

扩展：可以试试连接数据库存储用户信息？