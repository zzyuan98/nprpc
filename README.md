# nprpc
a rpc framework base on Protobuf , Zookeeper , Netty  developed by Java



项目介绍与流程图：

技术栈
- 使用Protobuf做序列化与反序列化 , 参数协议：header_size + 服务对象和方法 + 方法形参列表
- zookeeper做服务注册中心
- 底层使用netty做网络通信
- ThreadLocal
- 事件回调机制
- Maven

功能：RpcConsumer（服务调用方）对RpcProvider（服务提供方）发起 rpc调用 ，模拟实现了简单用户信息的CRUD操作（登录注册等等）

![image](https://user-images.githubusercontent.com/75787932/169328079-85631171-8bb6-4632-84f4-bc2d16e3d6ba.png)

![image](https://user-images.githubusercontent.com/75787932/169338704-60abdca0-34f5-4060-bece-a1822ddb34a8.png)

如何启动：
 ```txt
  - clone project 到 idea
  - 安装好zookeeper，启动zookeeper
  - zookeeper的配置信息在config.properties文件中
  - 启动RpcProvider项目下的 com.fixbug.provider.RpcProvider.App类下面的main方法
  - 启动RpcConsumer项目下的 com.fixbug.consumer.RpcConsumer.App类下面的main方法
  ```
  
  项目难点：底层调用机制，特别是复杂的事件回调机制的代码
