package com.fixbug.provider;

import com.fixbug.RpcMetaProto;
import com.fixbug.callback.INotifyProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


/**
 * rpc 服务器端使用netty开发
 *
 * @author zzyuan
 * @create 2022-04-06 21:29
 */
public class RpcServer {

    private INotifyProvider notifyProvider;


    public RpcServer(INotifyProvider notify){
        this.notifyProvider = notify;
    }

    public void start(String ip , int port){

        //创建主事件循环对应IO线程主要用于处理新用户的连接事件
        EventLoopGroup mainGroup = new NioEventLoopGroup(1);

        //创建work工作线程循环事件,主要用来创建已连接用户的可读写事件
        NioEventLoopGroup workGroup = new NioEventLoopGroup(3);

        //netty网络服务的启动辅助类
        ServerBootstrap b = new ServerBootstrap();
        b.group(mainGroup,workGroup)
                .channel(NioServerSocketChannel.class)//底层使用java NIO select模型
                .option(ChannelOption.SO_BACKLOG,1024)//设置tcp参数
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        /**
                         * 1.设置数据的编码和解码器
                         *   网络的字节流《——》业务要处理的数据类型
                         * 2.设置具体的处理器回调
                         *
                         */
                        channel.pipeline().addLast(new ObjectEncoder());//编码
                        channel.pipeline().addLast(new RpcServerChannel());//设置事件回调处理器
                    }
                });//注册事件回调，把业务层的代码和网络层的代码区分开来

        try {
            //阻塞，开启网络服务
            ChannelFuture f = b.bind(ip, port).sync();
            //关闭网络服务
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
            mainGroup.shutdownGracefully();
        }

    }


    /**
     * 继承自netty的ChannelInboundHandlerAdapter适配器
     * 主要提供相应的回调操作
     */
    private class RpcServerChannel extends ChannelInboundHandlerAdapter{

        /**
         * 处理接收到的事件
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            /**
             * ByteBuf(netty) 类似java nio的ByteBuffer
             * request 就是远程发送过来的所有的rpc调用请求包含的消息参数
             *
             * head_size + UserServiceRpcLoginzhangsan123456(LoginRequest)
             *  20 + UserServiceRpcLogin + 参数数据
             *
             */
            ByteBuf request = (ByteBuf) msg;

            //1.先读取头部信息的长度
            int header_size = request.readInt();//20

            //2.读取头部信息包含的服务对象名称和服务方法名称
            byte[] metabuf = new byte[header_size];//UserServiceRpcLogin
            request.readBytes(metabuf);

            //3.反序列化生成RpcMeta
            RpcMetaProto.RpcMeta rpcMeta = RpcMetaProto.RpcMeta.parseFrom(metabuf);
            String serviceName = rpcMeta.getServiceName();
            String methodName = rpcMeta.getMethodName();

            //4.读取rpc方法的参数
            byte[] argbuf = new byte[request.readableBytes()];
            request.readBytes(argbuf);

            //5.上面这些参数
            //serverName methodName argbuf

            byte[] response = notifyProvider.notify(serviceName,methodName,argbuf);


            //6.把rpc方法调用的相应response通过网络发送给rpc调用方
            ByteBuf buf = Unpooled.buffer(response.length);
            buf.writeBytes(response);
            ChannelFuture f = ctx.writeAndFlush(buf);

            //7.模拟http响应完成后，直接关闭连接
            if(f.sync().isSuccess()){
                ctx.close();
            }


        }

        /**
         * 连接异常处理
         * @param ctx
         * @param cause
         * @throws Exception
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

}
