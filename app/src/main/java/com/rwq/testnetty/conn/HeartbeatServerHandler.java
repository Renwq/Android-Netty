package com.rwq.testnetty.conn;

import android.util.Log;

import com.rwq.testnetty.bean.Message;
import com.rwq.testnetty.event.ChatBaseEvent;
import com.rwq.testnetty.event.ChatTransDataEvent;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatServerHandler extends SimpleChannelInboundHandler<Message.IMMessage> {
    private  final String TAG = getClass().getSimpleName();
    private ChatBaseEvent chatBaseEvent;
    private ChatTransDataEvent chatTransDataEvent;

    public HeartbeatServerHandler(ChatBaseEvent chatBaseEvent, ChatTransDataEvent chatTransDateEvent) {
        this.chatBaseEvent = chatBaseEvent;
        this.chatTransDataEvent = chatTransDateEvent;
    }

    //利用写空闲发送心跳检测消息
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Log.i(TAG, "userEventTriggered");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                Message.IMPingMessage pingMessage = Message.IMPingMessage.newBuilder()
                        .setMsgId(System.currentTimeMillis()+"")
                        .setSource("ANDROID")
                        .setUserId("20201332001000001").build();
                Message.IMMessage pingMsg = Message.IMMessage.newBuilder()
                        .setDataType(Message.IMMessage.DataType.IMPingMessage)
                        .setPingMessage(pingMessage).build();
                ctx.writeAndFlush(pingMsg);
                Log.i(TAG, "发送心跳：" + pingMsg);
            }
        }
    }

    //连接成功触发channelActive
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "channelActive");
        chatBaseEvent.onLoginMessage(ImConnProvide.CONNECT_CODE);
    }

    //断开连接触发channelInactive
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "channelInactive");
        chatBaseEvent.onLinkCloseMessage(ImConnProvide.DISCONNECT_CODE);
    }

    //客户端收到消息
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message.IMMessage message) throws Exception {
        Log.i(TAG, "channelRead0:"+message.getDataType());
        switch (message.getDataType()) {
            case IMAuthMessage:
                Log.i(TAG, "channelRead0:"+message.getAuthMessage());
                break;
            case IMAuthMessageAck:
                Log.i(TAG, "channelRead0:"+message.getAuthMessageAck());
                break;
            case IMChatMessage:
                Log.i(TAG, "channelRead0:"+message.getChatMessage().getBody());
                break;
            case IMPingMessage:
                Log.i(TAG, "channelRead0:"+message.getPingMessage());
                break;
            case IMPongMessage:
                Log.i(TAG, "channelRead0:" + message.getPongMessage());
                break;
            case IMChatMessageACK:
                Log.i(TAG, "channelRead0:" + message.getChatMessageAck());
                break;
            case IMOffLineMessage:
                Log.i(TAG, "channelRead0:" + message.getOffLineMessage());
                break;
            case IMGroupChatMessage:
                Log.i(TAG, "channelRead0:" + message.getGroupChatMessage().getBody());
                break;
            default:
                Log.i(TAG, "channelRead0:" + message.getUnknownFields());
                break;
        }
        //通过接口将值传出去
        //chatTransDataEvent.onTransBuffer(byteBuf);
    }

    //异常回调,默认的exceptionCaught只会打出日志，不会关掉channel
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.i(TAG, "exceptionCaught");
        chatBaseEvent.onLinkCloseMessage(ImConnProvide.DISCONNECT_CODE);
        cause.printStackTrace();
        ctx.close();
    }
}
