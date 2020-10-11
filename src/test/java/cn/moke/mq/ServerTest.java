
package cn.moke.mq;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;

import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.server.NettyServer;
import cn.moke.mq.server.RequestHandler;
import cn.moke.mq.server.handlers.FetchRequestHandler;
import cn.moke.mq.server.handlers.ProducerRequestHandler;
import cn.moke.mq.server.handlers.ReplicaRequestHandler;


public class ServerTest {
	
	@Before
	public void before(){
		BasicConfigurator.configure();
	}

    @Test
    public void testCreateServer() {
        
    }
    
    public static void main(String[] args) throws InterruptedException{
        NettyServer nettyServer = new NettyServer();
        Properties config = new Properties();
        config.setProperty("mq.host", "192.168.1.43");
        config.setProperty("mq.port", "9000");
        config.setProperty("mq.replica.host", "192.168.7.131");
//        config.setProperty("replica.master", "192.168.7.131");
//        config.setProperty("replica.hosts", "192.168.7.131");
        config.setProperty("mq.log.dir", "./data");
        config.setProperty("mq.enable.zookeeper", "true");
        config.setProperty("mq.zk.connect", "192.168.1.14:2181");
        config.setProperty("mq.zk.username", "admin");
        config.setProperty("mq.zk.password", "password");
        config.setProperty("mq.active.broker.url", "tcp://192.168.1.13:61616");
        ServerConfig serverConfig = new ServerConfig(config);
        nettyServer.start(serverConfig);
		nettyServer.registerHandler(RequestHandler.FETCH, new FetchRequestHandler(serverConfig));
		nettyServer.registerHandler(RequestHandler.PRODUCER, new ProducerRequestHandler());
		nettyServer.registerHandler(RequestHandler.REPLICA, new ReplicaRequestHandler(serverConfig));
		nettyServer.waitForClose();
    }
}
