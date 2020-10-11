package cn.moke.mq.client.producer;

import static cn.moke.mq.util.ZkUtils.ZK_MQ_BASE;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.client.NettyClient;
import cn.moke.mq.cluster.Broker;
import cn.moke.mq.cluster.Cluster;
import cn.moke.mq.cluster.Group;
import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.exception.SendRequestException;
import cn.moke.mq.exception.TimeoutException;
import cn.moke.mq.exception.ZkException;
import cn.moke.mq.network.Message;
import cn.moke.mq.network.Topic;
import cn.moke.mq.network.TransferType;
import cn.moke.mq.server.RequestHandler;
import cn.moke.mq.server.ServerRegister;
import cn.moke.mq.util.AMQUtils;
import cn.moke.mq.util.DataUtils;
import cn.moke.mq.util.ZkUtils;
import cn.moke.mq.zk.ZkChildListener;
import cn.moke.mq.zk.ZkClient;

public class Producer {
	
	private static final String ZK_PRODUCER_REGISTER_PATH = "/producer";
	
	private static final Producer INSTANCE = new Producer();
	
	private final static Logger LOGGER = LoggerFactory.getLogger(Producer.class);
	
	private NettyClient client = null;
	
	public ZkClient zkClient = null;
	
	private String host = null;
	
	private boolean activemq = true;
	
	private String topic = null;
	
	/**
     * 错误队列
     */
    private BlockingQueue<Topic> errorQueue = new LinkedBlockingQueue<Topic>();
	
	private Producer(){}
	
	public static Producer getInstance(){
		return INSTANCE;
	}
	
	public  void connect(String path) throws ConnectException{
		if(client == null){
			File mainFile = null;
			try {
				URL url = new URL(path);
				mainFile = new File(url.getFile()).getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (!mainFile.isFile() || !mainFile.exists()) {
				LOGGER.error(String.format("ERROR: Main config file not exist => '%s', copy one from 'conf/server.properties.sample' first.", mainFile.getAbsolutePath()));
	            System.exit(2);
	        }
	        final ServerConfig config = new ServerConfig(mainFile);
	        connect(config);
		}
	}
	
	
	public  void connect(ServerConfig config) throws ConnectException{
		if(client == null){
			client = new NettyClient();
			if(config.getEnableZookeeper()){
				this.zkClient = client.initZkClient(config);
				loadClusterFromZK(config);
				client.zkClient.subscribeChildChanges(ServerRegister.ZK_BROKER_GROUP,  new ZkChildListener(){
					@Override
					public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
						ZkUtils.getCluster(client.zkClient);
					}
				});
			}else{
				Group brokerGroup = new Group(config.getBrokerGroupName(), config.getHost(), config.getPort());
				Cluster.setCurrent(brokerGroup);
			}
			host = config.getHost();
			if(StringUtils.isNotBlank(config.getActiveBrokerUrl())){
				AMQUtils.getInstance().setBrokerUrl(config.getActiveBrokerUrl());
				this.topic = config.getTopic();
			}
		}
	}
	
	public boolean reConnect(){
		int count = 0;
		do{
			String path = ZkUtils.ZK_MQ_BASE + ServerRegister.AMQ_PATH;
			if(zkClient.exists(path)){
				String val = ZkUtils.readData(zkClient, path);
				if("yes".equals(val)){
					String url = ZkUtils.readData(zkClient, ZK_MQ_BASE + ServerRegister.AMQ_PATH + "/path");
					if(StringUtils.isNotBlank(url)){
						AMQUtils.getInstance().setBrokerUrl(url);
					}
					activemq = true;
				}else{
					activemq = false;
				}
			}
			if(activemq){
				return true;
			}
			
			Group group = Cluster.peek();
			if(null == group){
				return false;
			}
			Broker master = group.getMaster();
			try {
				client.open(master.getHost(), master.getPort());
				String clientPath = ZkUtils.ZK_MQ_BASE + ZK_PRODUCER_REGISTER_PATH;
				if(StringUtils.isBlank(host)){
		    		InetAddress addr = InetAddress.getLocalHost();
		    		host = addr.getHostAddress().toString();//获得本机IP
		    	}
				ZkUtils.registerClient(client.zkClient, clientPath, host, "");
				break;
			}catch (IllegalStateException e) {
				client.stop();
				client = new NettyClient();
				client.zkClient = this.zkClient;
				count++;
				ZkUtils.getCluster(client.zkClient);
				LOGGER.error(String.format("producer connect %s:%d error：", master.getHost(), master.getPort()));
			} catch (ZkException e) {
				LOGGER.error("zk error", e);
			} catch (Exception e) {
				count++;
				ZkUtils.getCluster(client.zkClient);
				LOGGER.error(String.format("producer connect %s:%d error：", master.getHost(), master.getPort()));
			}
		}while(count < 2 && !client.connected);
		return client.connected;
	}
	
	public boolean send(Topic topic){
		return send(new Topic[]{topic});
	}
	
	public boolean send(Topic topic, String... topicNames){
		List<Topic> topics = new ArrayList<Topic>();
		if(null != topicNames){
			for(String tp:topicNames){
				Topic top = new Topic();
				top.setTopic(tp);
				top.getContents().addAll(topic.getContents());
				topics.add(top);
			}
		}
		return send(topics);
	}
	
	public boolean send(Topic[] topics){
		return send(Arrays.asList(topics));
	}
	
	public boolean send(List<Topic> topics) throws SendRequestException{
		boolean result = false;
		if(reConnect()){
			if(activemq){
				AMQUtils.getInstance().sendMessage(topics, topic);
			}else{
				Message request = Message.newRequestMessage();
				request.setReqHandlerType(RequestHandler.PRODUCER);
				request.setBody(DataUtils.serialize(topics));
				boolean errorFlag = false;
				try {
					Message response = client.write(request);
					if (response == null || response.getType() == TransferType.EXCEPTION.value) {
						errorQueue.addAll(topics);
						result = false;
						errorFlag = true;
					} else {
						result = true;
					}
				} catch (TimeoutException e) {
					client = new NettyClient();
					errorQueue.addAll(topics);
					errorFlag = true;
					if(!reConnect()){
						throw new SendRequestException("Prouder connection error");
					}
				} catch (SendRequestException e) {
					client = new NettyClient();
					errorQueue.addAll(topics);
					errorFlag = true;
					if(!reConnect()){
						throw new SendRequestException("Prouder connection error");
					}
				}
				if(!errorFlag){
					if(errorQueue.size() > 0){
						try {
							for(int i=0;i<5;i++){
								Topic t = errorQueue.poll(100, TimeUnit.MILLISECONDS);
								if(t != null){
									send(t);
								}else{
									break;
								}
							}
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
			errorQueue.addAll(topics);
		}
		return result;
	}
	
	
	public void stop() {
		client.stop();
	}
	
	
	private void loadClusterFromZK(ServerConfig config){
		if(config.getEnableZookeeper()){
			ZkUtils.getCluster(client.zkClient);
		}
	}
	
	
	
	

}
