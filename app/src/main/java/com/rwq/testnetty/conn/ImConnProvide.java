package com.rwq.testnetty.conn;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.rwq.testnetty.bean.Message;
import com.rwq.testnetty.event.ChatBaseEvent;
import com.rwq.testnetty.event.ChatTransDataEvent;
import com.rwq.testnetty.event.MessageQoSEvent;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class ImConnProvide {
    public static final int CONNECT_CODE = 1;
    public static final int DISCONNECT_CODE = -1;
    private static final Integer CONNECT_TIMEOUT = 5000;
    private static ImConnProvide imConnProvide;
    private boolean isInit = false;
    private Bootstrap bootstrap;
    private String host;
    private int tcpProt;
    private Channel channel;
    private ChannelFuture channelFuture;
    private MessageQoSEvent mMessageQosEvent;
    private ChatBaseEvent mChatBaseEvent;
    private ChatTransDataEvent mChatTransDateEvent;
    private EventLoopGroup group;


    private ImConnProvide() {

    }

    public static ImConnProvide getInstance() {
        synchronized (ImConnProvide.class) {
            if (imConnProvide == null) {
                imConnProvide = new ImConnProvide();
            }
            return imConnProvide;
        }
    }


    public void init(String host, int tcpProt, ChatBaseEvent chatBaseEvent, ChatTransDataEvent chatTransDataEvent, MessageQoSEvent messageQoSEvent) {
        if (host == null || tcpProt == 0) {
            throw new IllegalArgumentException("连接地址不能为null");
        }
        isInit = true;
        this.host = host;
        this.tcpProt = tcpProt;
        this.mChatBaseEvent = chatBaseEvent;
        this.mChatTransDateEvent = chatTransDataEvent;
        this.mMessageQosEvent = messageQoSEvent;
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap().group(group)
                //禁用nagle算法 Nagle算法就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。
                .option(ChannelOption.TCP_NODELAY, true)//屏蔽Nagle算法试图
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
                //指定NIO方式   //指定是NioSocketChannel, 用NioSctpChannel会抛异常
                .channel(NioSocketChannel.class)
                //指定编解码器，处理数据的Handler
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast("protobufVarint32FrameDecoder", new ProtobufVarint32FrameDecoder());
                        socketChannel.pipeline().addLast("protobufDecoder", new ProtobufDecoder(Message.IMMessage.getDefaultInstance()));
                        socketChannel.pipeline().addLast("protobufVarint32LengthFieldPrepender", new ProtobufVarint32LengthFieldPrepender());
                        socketChannel.pipeline().addLast("protobufEncoder", new ProtobufEncoder());
                        socketChannel.pipeline().addLast(new IdleStateHandler(0, 0, 3, TimeUnit.SECONDS));
                        socketChannel.pipeline().addLast(new HeartbeatServerHandler(mChatBaseEvent, mChatTransDateEvent));
                    }
                });

    }


    public void release() {
        isInit = false;
        disconnect();
        host = null;
        tcpProt = 0;
        mChatBaseEvent = null;
        mChatTransDateEvent = null;
        mMessageQosEvent = null;
        group = null;
        bootstrap = null;
        channelFuture = null;
    }

    public void connect() {
        if (bootstrap == null || TextUtils.isEmpty(host) || tcpProt == 0) {
            throw new NullPointerException("连接之前请初始化");
        }
        try {
            channelFuture = bootstrap.connect(host, tcpProt).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        //连接成功
                        channel = channelFuture.channel();
                    } else {
                        //连接失败
                        if (mChatBaseEvent != null) {
                            mChatBaseEvent.onLoginMessage(DISCONNECT_CODE);
                        }
                    }
                }
            }).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (null != channelFuture) {
                if (channelFuture.channel() != null && channelFuture.channel().isOpen()) {
                    channelFuture.channel().close().sync();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (group != null) {
                group.shutdownGracefully();
            }
        }
    }


    @Nullable
    public Channel getChannel() {
        return channel;
    }
}
