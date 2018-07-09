package com.jin2ml.nettytest.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * netty5 服务端
 *
 * @author jin2ml
 * @date 2018/7/6 19:01
 */
public class Server {

    public static void main(String[] args) {

        //boss和worker
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            //服务类
            ServerBootstrap bootstrap = new ServerBootstrap();

            //设置线程池
            bootstrap.group(boss, worker);

            //设置socket工厂
            bootstrap.channel(NioServerSocketChannel.class);

            //设置管道工厂
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    channel.pipeline().addLast(new StringEncoder());
                    channel.pipeline().addLast(new StringDecoder());
                    channel.pipeline().addLast(new ServerHandler());
                }
            });

            //设置参数,TCP参数
            //设置serverSocketChannel，最大连接缓冲数量
            bootstrap.option(ChannelOption.SO_BACKLOG, 2048);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);

            //绑定端口
            ChannelFuture future = bootstrap.bind(11111);

            System.out.println("start...");

            //等待服务关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放资源
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
