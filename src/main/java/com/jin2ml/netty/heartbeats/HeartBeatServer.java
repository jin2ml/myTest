package com.jin2ml.netty.heartbeats;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 服务端消息处理
 *
 * @author jin2ml
 * @date 2018-07-09
 */
public class HeartBeatServer {

    /**
     * 端口
     */
    private int port;

    private HeartBeatServer(int port) {
        this.port = port;
    }

    /**
     * 检测chanel是否接受过心跳数据时间间隔（单位秒）
     */
    private static final int READ_WAIT_SECONDS = 10;

    private void startServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new HeartBeatServerInitializer());

            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_BACKLOG, 2048);
            // 服务器绑定端口监听
            ChannelFuture f = bootstrap.bind(port);
            // 监听服务器关闭监听，此方法会阻塞
            System.out.println("service start...");
            f.channel().closeFuture().sync();
            // 可以简写为
            /* bootstrap.bind(portNumber).sync().channel().closeFuture().sync(); */
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 消息处理器
     *
     * @author cullen edward
     */
    private class HeartBeatServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            /*
             * 使用ObjectDecoder和ObjectEncoder
             * 因为双向都有写数据和读数据，所以这里需要两个都设置
             * 如果只读，那么只需要ObjectDecoder即可
             */
            pipeline.addLast("decoder", new StringDecoder());
            pipeline.addLast("encoder", new StringEncoder());

            /*
             * 这里只监听读操作
             * 可以根据需求，监听写操作和总得操作
             */
            pipeline.addLast("pong", new IdleStateHandler(READ_WAIT_SECONDS, 0, 0, TimeUnit.SECONDS));

            pipeline.addLast("handler", new HeartbeatHandler());
        }
    }

    /**
     * @param args args
     */
    public static void main(String[] args) {
        HeartBeatServer heartbeatServer = new HeartBeatServer(9597);
        heartbeatServer.startServer();
    }
}
