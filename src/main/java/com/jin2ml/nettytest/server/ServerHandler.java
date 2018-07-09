package com.jin2ml.nettytest.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;

/**
 * @author jin2ml
 * @date 2018/7/6 19:01
 */
public class ServerHandler extends SimpleChannelInboundHandler<String>{

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println(s);
        //回写给客户端
        ctx.channel().writeAndFlush("receive");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive...");
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("connect...");
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("disconnect...");
    }


}
