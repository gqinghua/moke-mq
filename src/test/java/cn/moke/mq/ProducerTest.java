
package cn.moke.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import cn.moke.mq.client.producer.Producer;
import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.network.Topic;





public class ProducerTest {
	
	@Before
	public void before(){
		BasicConfigurator.configure();
	}

    @Test
    public void testProducer() throws InterruptedException {
    	try {
    		Properties config = new Properties();
            config.setProperty("enable.zookeeper", "true");
            config.setProperty("zk.connect", "192.168.1.14:2181");
            config.setProperty("zk.username", "admin");
            config.setProperty("zk.password", "password");
    		config.setProperty("port", "9000");
            ServerConfig serverConfig = new ServerConfig(config);
			Producer.getInstance().connect(serverConfig);
			List<Topic> list = new ArrayList<Topic>();
			Topic topic = new Topic();
			topic.setTopic("test");
			topic.addContent("sssssssssssssssssssssss");
			list.add(topic);
			Producer.getInstance().send(list);
			
			Producer.getInstance().stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static void main(String[] args){
//    	String cfg = "file:/gitlib/uncode-mq/conf/config.properties";
    	try {
    		Properties config = new Properties();
    		config.setProperty("mq.port", "9000");
            config.setProperty("mq.zk.connect", "192.168.1.14:2181");
            config.setProperty("mq.enable.zookeeper", "true");
            config.setProperty("mq.zk.username", "admin");
            config.setProperty("mq.zk.password", "password");
            
            //config.setProperty("hostname", "127.0.0.1");
    		ServerConfig serverConfig = new ServerConfig(config);
            Producer.getInstance().connect(serverConfig);
            for(int i=0;i<10000;i++){
            	List<Topic> list = new ArrayList<Topic>();
    			Topic topic = new Topic();
    			topic.setTopic("zhengshi#CONSUMEREXPRESSRECORD");
    			topic.addContent("sssssssssssssssssssssss=>"+i);
    			list.add(topic);
    			Producer.getInstance().send(list);
            }
			Producer.getInstance().stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
