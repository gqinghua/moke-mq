package cn.moke.mq.client.consumer;

import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.config.ServerConfig;

public class ConsumerRunnable extends Thread{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerRunnable.class);
	
	public static int SLEEP_TIME = 200;
	
	private boolean closed = false;
	
	
	ConsumerRunnable(ServerConfig config) throws ConnectException{
		super("uncode-mq-consumer");
		Consumer.getInstance().connect(config);
	}
	
    @Override
    public void run() {
    	LOGGER.info("consumer fetch start ");
    	try {
    		//解决启动报错问题
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
		}
	    try {
	        while (!closed) {
	        	try{
	        		Consumer.fetch();
	        	}catch (Exception e) {
	    	    	LOGGER.error("consumer fetch error ", e);
	    	    }
	        	Thread.sleep(SLEEP_TIME);
	        }
	    }catch (InterruptedException e) {
	    	LOGGER.error("error in consumer runnable ", e);
	    }
    }
    
    public void start(){
    	super.start();
    }
	
	public void close(){
		closed = true;
		this.interrupt();
	}
	
	
	
	
	

}
