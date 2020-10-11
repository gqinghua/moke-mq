package cn.moke.mq.client.consumer;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.client.NettyClient;
import cn.moke.mq.cluster.Broker;
import cn.moke.mq.cluster.Cluster;
import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.exception.SendRequestException;
import cn.moke.mq.exception.TimeoutException;
import cn.moke.mq.exception.ZkNodeExistsException;
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

public class Consumer{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
	
	private static final String ZK_CONSUMER_REGISTER_PATH = "/consumer";
	
	private static final Consumer INSTANCE = new Consumer();
	private static int  ZK_COUNER_MAX = 5;
	private static int NULL_COUNTER = 0;
	private static Broker BROKER = null;
	private static boolean CHANGE_CLIENT = false;
	
	private Lock lock = new ReentrantLock(true);
	private Random random = new Random();
	private Set<ConsumerSubscriber> subscribers = new HashSet<ConsumerSubscriber>();
	private Set<String> topics = new HashSet<String>();
	public ZkClient zkClient = null;
	private int zkCounter = 0;
	private String host = null;

	NettyClient client = new NettyClient();
	private ConsumerRunnable consumerRunnableThread;
	
	private Consumer(){}
	
	public static Consumer getInstance(){
		return INSTANCE;
	}
	
	public Set<ConsumerSubscriber> getSubscribers() {
		return subscribers;
	}

	public void connect(ServerConfig config) throws ConnectException{
		if(config.getEnableZookeeper()){
			INSTANCE.zkClient = INSTANCE.client.initZkClient(config);
			INSTANCE.zkClient.subscribeChildChanges(ServerRegister.ZK_BROKER_GROUP, new ZkChildListener(){
				@Override
				public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
					ZkUtils.getCustomerCluster(INSTANCE.zkClient, INSTANCE.topics.toArray(new String[0]));
				}
			});
		}
		if(config.getTopics() != null){
			for(String topic:config.getTopics()){
				INSTANCE.topics.add(topic);
			}
		}
		INSTANCE.host = config.getHost();
		ZK_COUNER_MAX = config.getZKDataPersistenceInterval()/2;
		INSTANCE.zkCounter = ZK_COUNER_MAX;
		if(config.getEnableConsumer()){
			String path = ZkUtils.ZK_MQ_BASE + ServerRegister.AMQ_PATH;
			if(zkClient.exists(path)){
				String val = ZkUtils.readData(zkClient, path);
				if("yes".equals(val)){
//					String url = ZkUtils.readData(zkClient, ZK_MQ_BASE + ServerRegister.AMQ_PATH + "/path");
					if(StringUtils.isNotBlank(config.getActiveBrokerUrl())){
						AMQUtils.getInstance().setBrokerUrl(config.getActiveBrokerUrl());
						AMQUtils.getInstance().registerMessageListener(config.getTopic());
					}
				}
			}
		}
		
	}
	
	public boolean reConnect(){
		if(!client.connected){
			if(client != null && client.zkClient != null){
				if(INSTANCE.zkCounter >= ZK_COUNER_MAX){
					ZkUtils.getCustomerCluster(client.zkClient, topics.toArray(new String[0]));
					INSTANCE.zkCounter = 0;
					CHANGE_CLIENT = true;
				}
				if(BROKER == null){
					Map<Broker, List<String>> ipWithTopics = Cluster.getCustomerServerByQueues(topics.toArray(new String[0]));
					Broker[] brokers = ipWithTopics.keySet().toArray(new Broker[0]);
					if(brokers != null && brokers.length > 0){
						BROKER = brokers[0];
					}
					CHANGE_CLIENT = true;
				}
				if(BROKER != null){
					if(CHANGE_CLIENT){
						client.stop();
						client = new NettyClient();
						client.zkClient = zkClient;
						CHANGE_CLIENT = false;
					}
					
					try {
						client.open(BROKER.getHost(), BROKER.getPort());
				    	String clientPath = ZkUtils.ZK_MQ_BASE + ZK_CONSUMER_REGISTER_PATH;
				    	if(StringUtils.isBlank(host)){
				    		InetAddress addr = InetAddress.getLocalHost();
				    		host = addr.getHostAddress().toString();//获得本机IP
				    	}
						ZkUtils.registerClient(zkClient, clientPath, host, topics.toString());
					} catch (IllegalStateException e) {
						client.connected = false;
						INSTANCE.zkCounter++;
						LOGGER.error(String.format("consumer %s:%d error：", BROKER.getHost(), BROKER.getPort()));
					} catch (ZkNodeExistsException e) {
						LOGGER.error("zk error", e);
					} catch (Exception e) {
						client.connected = false;
						LOGGER.error(String.format("consumer %s:%d error：", BROKER.getHost(), BROKER.getPort()));
					}
					int last = (int) (System.currentTimeMillis()%10);
					if(last > 5){
						INSTANCE.zkCounter++;
					}
				}else{
					INSTANCE.zkCounter++;
				}
			}
		}
		return client.connected;
	}
	
	public static void fetch(){
		if(INSTANCE.topics.size() > 0){
			if(INSTANCE.reConnect()){
				try {
					INSTANCE.fetch(INSTANCE.topics.toArray(new String[0]));
				} catch (Exception e) {
					INSTANCE.client.connected = false;
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}
	
	public List<Topic> fetch(String[] topics)throws TimeoutException, SendRequestException{
		return fetch(Arrays.asList(topics));
	}
	
	public List<Topic> fetch(List<String> topics) throws TimeoutException, SendRequestException{
		List<Topic> rtTopics = null;
		if(topics != null && topics.size() > 0){
			List<Topic> topicList = new ArrayList<Topic>();
			for(String tp : topics){
				Topic topic = new Topic();
				topic.setTopic(tp);
				topicList.add(topic);
			}
			Message request = Message.newRequestMessage();
			request.setReqHandlerType(RequestHandler.FETCH);
			request.setBody(DataUtils.serialize(topicList));
			try {
				Message response = client.write(request);
				if (response.getType() == TransferType.EXCEPTION.value) {
					// 有异常
					LOGGER.error("Cuonsumer fetch message error");
				} else {
					if(null != response.getBody()){
						rtTopics = (List<Topic>) DataUtils.deserialize(response.getBody());
					}
				}
			} catch (TimeoutException e) {
				throw e;
			} catch (SendRequestException e) {
				throw e;
			}
			
			//通知订阅者
			if(rtTopics != null){
				LOGGER.info("=>> Consumer fetch message:"+rtTopics.toString());
				lock.lock();
				try {
					for(Topic topic : rtTopics){
						for (ConsumerSubscriber subscriber : INSTANCE.subscribers) {
							if(subscriber.subscribeToTopic() != null && subscriber.subscribeToTopic().contains(topic.getTopic())){
								try {
									subscriber.notify(topic);
								} catch (Exception e) {
									LOGGER.error("Consumer notify error", e);
								}
							}
						}
					}
				} finally {
					lock.unlock();
				}
				NULL_COUNTER = 0;
				ConsumerRunnable.SLEEP_TIME = 200;
			}else{
				NULL_COUNTER++;
				if(NULL_COUNTER > 5){
					Set<String> ips = ZkUtils.getNotSpendingTopics(null, topics.toArray(new String[0]));
					if(ips != null && ips.size() > 0){
						String[] iprandmon = ips.toArray(new String[0]);
						int index = 0;
						if(ips.size() > 1){
							index = random.nextInt(ips.size());
						}
						String ip = iprandmon[index];
						if(BROKER != null && !ip.equals(BROKER.getHost())){
							BROKER = new Broker(ip, BROKER.getPort());
							INSTANCE.zkCounter = ZK_COUNER_MAX;
							client.connected = false;
						}
					}else{
						if(BROKER != null){
							Map<Broker, List<String>> ipWithTopics = Cluster.getCustomerServerByQueues(topics.toArray(new String[0]));
							Broker[] brokers = ipWithTopics.keySet().toArray(new Broker[0]);
							if(brokers != null && brokers.length > 0){
								if(!BROKER.getHost().equals(brokers[0].getHost())){
									BROKER = null;
									INSTANCE.zkCounter = ZK_COUNER_MAX;
									client.connected = false;
								}else{
									INSTANCE.zkCounter++;
								}
							}else{
								INSTANCE.zkCounter++;
							}
							if(INSTANCE.zkCounter >= ZK_COUNER_MAX){
								client.connected = false;
								BROKER = null;
							}
						}
					}
					ConsumerRunnable.SLEEP_TIME = 1000;
					NULL_COUNTER = 0;
				}
			}
		}
		return rtTopics;
	}
	
	public void stop() {
		client.stop();
	}
	
	public static void addSubscriber(ConsumerSubscriber subscriber){
		INSTANCE.lock.lock();
		try {
			if (subscriber != null){
				if(!INSTANCE.subscribers.contains(subscriber)){
					INSTANCE.subscribers.add(subscriber);
				}
				INSTANCE.topics.addAll(subscriber.subscribeToTopic());
			}
		} finally {
			INSTANCE.lock.unlock();
		}
	}
	
	public static void deleteSubscriber(ConsumerSubscriber subscriber) {
		INSTANCE.lock.lock();
		try {
			if (subscriber != null){
				if(INSTANCE.subscribers.contains(subscriber)){
					INSTANCE.subscribers.remove(subscriber);
				}
				INSTANCE.topics.removeAll(subscriber.subscribeToTopic());
			}
		} finally {
			INSTANCE.lock.unlock();
		}
	}
	
	public static void runningConsumerRunnable(String path) throws ConnectException{
		File mainFile = null;
		try {
			URL url = new URL(path);
			mainFile = new File(url.getFile()).getCanonicalFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!mainFile.isFile() || !mainFile.exists()) {
            System.err.println(String.format("ERROR: Main config file not exist => '%s', copy one from 'conf/server.properties.sample' first.", mainFile.getAbsolutePath()));
            System.exit(2);
        }
		ServerConfig serverConfig = new ServerConfig(mainFile);
		if(serverConfig.getEnableConsumer()){
			runningConsumerRunnable(serverConfig);
		}
	}
	
	public static void runningConsumerRunnable(ServerConfig config) throws ConnectException{
		if(config.getEnableConsumer()){
			if(INSTANCE.consumerRunnableThread == null){
				INSTANCE.consumerRunnableThread = new ConsumerRunnable(config);
				INSTANCE.consumerRunnableThread.start();
			}
		}
		
	}

	
	
	

}
