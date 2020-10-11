package cn.moke.mq.server;

import cn.moke.mq.config.ServerConfig;

public abstract class AbstractRequestHandler implements RequestHandler {
	
	protected ServerConfig config;
	
	public AbstractRequestHandler(ServerConfig config){
		this.config = config;
	}

	

}
