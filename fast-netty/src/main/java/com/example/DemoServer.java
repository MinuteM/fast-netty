package com.example;

import com.core.dispatcher.MessageDispatcher;
import com.core.netty.bootstrap.server.ServerHandler;
import com.core.netty.bootstrap.server.TcpServer;

public class DemoServer extends TcpServer {



	@Override
	public void init() {
		
	}
	
	public static void main(String[] args) {
		MessageDispatcher.register(ServerHandler.class);
		new DemoServer().listen(9000).start();
	}

}
