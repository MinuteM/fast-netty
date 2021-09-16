package com.core.netty.bootstrap.server;

import cn.hutool.core.util.StrUtil;
import com.core.dispatcher.MessageDispatcher;
import com.core.dispatcher.disruptor.DisruptorManager;
import com.core.netty.coder.Message;
import com.core.session.Session;

import com.protobuf.Protobuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    public static final ConcurrentHashMap<ChannelId, Session> SESSIONS = new ConcurrentHashMap<ChannelId, Session>();

    private static ThreadLocal<DisruptorManager> LOCAL = new ThreadLocal<DisruptorManager>() {
        @Override
        protected DisruptorManager initialValue() {
            return new DisruptorManager() {
                @Override
                public void eventHandler(Message msg, long sequence) {
                    MessageDispatcher.dispatcher(msg);
                }
            };
        }
    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Protobuf.TestData data = Protobuf.TestData.parseFrom(msg.getBody());
        log.info(StrUtil.format("{}：{}", msg.getId(), data.getName()));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		System.err.println(ctx.channel().id().asLongText());
//		System.err.println(ctx.channel().id().asShortText());
//		System.err.println(ctx.channel().isActive());
//		System.err.println(ctx.channel().isOpen());
//		System.err.println("是服务器");
        Session session = new Session(ctx.channel());
        SESSIONS.put(ctx.channel().id(), session);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SESSIONS.remove(ctx.channel().id());
        super.channelInactive(ctx);
    }


}
