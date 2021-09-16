package com.example;

import com.core.dispatcher.MessageDispatcher;
import com.core.dispatcher.annotation.HandlerMsg;
import com.core.dispatcher.disruptor.DisruptorManager;
import com.core.netty.coder.Message;
import com.core.session.Session;
import com.google.protobuf.InvalidProtocolBufferException;
import com.protobuf.Protobuf;
import com.protobuf.Protobuf.TestData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentHashMap;

public class DomeServerHandler extends SimpleChannelInboundHandler<Message> {
	public static DomeServerHandler instance = new DomeServerHandler();

	public static DomeServerHandler getInstance() {
		return instance;
	}
	private long start;
	private long end;

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


	@HandlerMsg(id = 10)
	public void test1(Message msg) {
		try {
//			Thread.sleep(10);
			TestData data = Protobuf.TestData.parseFrom(msg.getBody());
			System.err.println(Thread.currentThread().getName()+" " +data.getId()+" " +msg.getSession().getChannel());
			if(data.getId() ==1) {
				start =System.currentTimeMillis();
			}
			if(data.getId() ==20000) {
				end =System.currentTimeMillis();
				System.err.println("start:"+start);
				System.err.println("start:"+end);
				System.err.println("v:"+(end-start));
				System.err.println(Thread.currentThread().getName()+" " +data.getId());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@HandlerMsg(id = 11)
	public void test2(Message msg) {
		try {
			TestData data = Protobuf.TestData.parseFrom(msg.getBody());
			System.err.println("消息11:"+data.getName()+"  "+ msg.getSession().getChannel());
			TestData.Builder db = TestData.newBuilder();
			db.setName(data.getName());
		    msg.getSession().writeAndFlush(new Message((short)11, db.build().toByteArray()));
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
		Session session =  SESSIONS.get(channelHandlerContext.channel().id());
		System.err.println(channelHandlerContext.channel().id().asLongText());
		if(session!= null) {
			message.setSession(session);
			LOCAL.get().publishEvent(message);
		}
	}
}
