package cn.moke.mq;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import cn.moke.mq.network.Message;
import cn.moke.mq.store.TopicQueue;
import cn.moke.mq.store.TopicQueuePool;

public class DiskTest {
	
	@Before
	public void before(){
		BasicConfigurator.configure();
		try {
			File logDir = new File("./bak");
			logDir = logDir.getCanonicalFile();
			if (!logDir.exists()) {
	            logDir.mkdirs();
	        }
	        if (!logDir.isDirectory() || !logDir.canRead()) {
	            throw new IllegalArgumentException(logDir.getAbsolutePath() + " is not a readable log directory.");
	        }
	        //TopicQueuePool.startup(logDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	@Test
    public void write(){
		Message message = new Message();
		
    	TopicQueue queue = TopicQueuePool.getQueue("demo");
		byte[] que = queue.poll();
    }

    @Test
    public void read(){
    	TopicQueue queue = TopicQueuePool.getQueue("demo");
		byte[] que = queue.poll();
    }
	
	public static void main(String[] args){
		
		try {
			File logDir = new File("./bak");
			logDir = logDir.getCanonicalFile();
			if (!logDir.exists()) {
	            logDir.mkdirs();
	        }
	        if (!logDir.isDirectory() || !logDir.canRead()) {
	            throw new IllegalArgumentException(logDir.getAbsolutePath() + " is not a readable log directory.");
	        }
	        
//			TopicQueue queue = new TopicQueue("demo", logDir.getAbsolutePath());
//			queue.offer("wwwwwwwwwwwwwwwwww".getBytes());
			
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
