package com.jin2ml.netty.heartbeats;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳实现类
 * @author jin2ml
 * @date 2018-07-09
 */
class HeartbeatHandler extends SimpleChannelInboundHandler<String> {
    /**
     * 失败计数器：未收到client端发送的ping请求
     */
    private int unRecPingTimes = 0;

    private String userid;

    /**
     * 定义服务端没有收到心跳消息的最大次数
     */
    private static final int MAX_UN_REC_PING_TIMES = 3;

    private static final String LOGIN = "LOGIN";
    private static final String HEARTBEAT = "HEARTBEAT";

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, String msg) throws Exception {
        //msg格式约定为"operation,userid"
        System.out.println("----->msg=" + msg);
        String[] args = msg.split(",");
        String msgOperation = args[0];
        String msgUserid = args[1];
        if (LOGIN.equals(msgOperation)) {
            if (msgUserid != null && msgUserid.length() > 0) {
                userid = msgUserid;
            }
            setUserOnlineStatus(userid, true);
        } else if (HEARTBEAT.equals(msgOperation)) {
            ctx.channel().writeAndFlush("success");
            // 失败计数器清零
            unRecPingTimes = 0;
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                /*读超时*/
                System.out.println("===服务端===(READER_IDLE 读超时) 次数：" + unRecPingTimes);
                // 失败计数器次数大于等于3次的时候，关闭链接，等待client重连
                if (unRecPingTimes >= MAX_UN_REC_PING_TIMES) {
                    System.out.println("===服务端===(读超时，关闭chanel)");
                    // 连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
                    ctx.channel().close();
                } else {
                    // 失败计数器加1
                    unRecPingTimes++;
                }
            } else if (event.state() == IdleState.WRITER_IDLE) {
                /*写超时*/
                System.out.println("===服务端===(WRITER_IDLE 写超时) 次数：" + unRecPingTimes);
            } else if (event.state() == IdleState.ALL_IDLE) {
                /*总超时*/
                System.out.println("===服务端===(ALL_IDLE 总超时) 次数：" + unRecPingTimes);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("错误原因：" + cause.getMessage());
        ctx.channel().close();
        setUserOnlineStatus(userid, false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client active ");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 关闭，等待重连
        ctx.close();
        System.out.println("===服务端===(客户端失效)");
        setUserOnlineStatus(userid, false);
    }

    /**
     * 设置用户在线离线状态
     */
    private void setUserOnlineStatus(String userid, boolean isOnline) {
        if (userid != null && !"".equals(userid)) {
            //更新用户信息为在线状态（此处代码省略）
            System.out.println("online:" + isOnline);
        }
    }
}
