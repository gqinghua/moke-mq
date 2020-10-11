package cn.moke.mq.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.client.consumer.Consumer;
import cn.moke.mq.client.consumer.ConsumerSubscriber;
import cn.moke.mq.network.Topic;


public class AMQUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(AMQUtils.class);
	
	private static final AMQUtils INSTANCE = new AMQUtils();
	
	// tcp 地址
	private String brokerUrl = "tcp://10.168.68.137:61616";
	private QueueConnection connection = null;
	private QueueSession session = null;
	private boolean register = false;
	
	private Map<String, Queue<Topic>> allTopics = new HashMap<>();
	
	
	public static AMQUtils getInstance(){
		return INSTANCE;
	}
	
	public void setBrokerUrl(String brokerUrl){
		this.brokerUrl = brokerUrl;
	}
	
	public List<Topic> fetch(String... topics){
		List<Topic> list = new ArrayList<>();
		if(topics != null){
			for(String topic:topics){
				Queue<Topic> tps = allTopics.get(topic);
				if(tps != null){
					Topic top = tps.poll();
					if(top != null){
						list.add(top);
					}
				}
			}
		}
		return list;
	}
	
	public void registerMessageListener(String topicName) {
		if(!register){
			if(session == null){
				connectActiveMQ();
			}
			if(session != null){
				try {
					// 创建一个消息队列
					javax.jms.Queue queue = session.createQueue(topicName);
		            // 创建消息制作者
		            javax.jms.QueueReceiver receiver = session.createReceiver(queue);
		            receiver.setMessageListener(new MessageListener() { 
		                public void onMessage(Message msg) { 
		                    if (msg != null) {
		                    	try {
		                    		TextMessage textMessage = (TextMessage) msg;
									Topic topic = JsonUtils.fromJson(textMessage.getText(), Topic.class);
									for (ConsumerSubscriber subscriber : Consumer.getInstance().getSubscribers()) {
										if(subscriber.subscribeToTopic() != null && subscriber.subscribeToTopic().contains(topic.getTopic())){
											try {
												subscriber.notify(topic);
											} catch (Exception e) {
												LOGGER.error(e.getMessage(), e);
											}
										}
									}
								} catch (JMSException e) {
									LOGGER.error("jms error", e);
								}
		                    }
		                } 
		            }); 
					// 提交会话
					session.commit();
					register = true;
				} catch (Exception e) {
					LOGGER.error("register jms error", e);
				}
			}
		}
		
	}

	/**
	 * <b>function:</b> 发送消息
	 * 
	 * @author hoojo
	 * @createDate 2013-6-19 下午12:05:42
	 * @param session
	 * @param producer
	 * @throws Exception
	 */
	public void sendMessage(List<Topic> topics, String topicName) {
		if(session == null){
			connectActiveMQ();
		}
		try {
			if (topics != null) {
				// 创建一个消息队列
	            javax.jms.Queue queue = session.createQueue(topicName);
				// 创建消息发送者
	            javax.jms.QueueSender sender = session.createSender(queue);
	            // 设置持久化模式
	            sender.setDeliveryMode(DeliveryMode.PERSISTENT);
				for (Topic topic : topics) {
					String json = JsonUtils.toJson(topic);
					TextMessage text = session.createTextMessage(json);
					sender.send(text);
				}
			}
			// 提交会话
			session.commit();
		} catch (Exception e) {
			closeActiveMQ();
			connectActiveMQ();
			LOGGER.error("sent amq message error.", e);
		}

	}

	public void connectActiveMQ() {
		try {
			// 创建链接工厂
			// 创建链接工厂
            QueueConnectionFactory factory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, brokerUrl);
            // 通过工厂创建一个连接
            connection = factory.createQueueConnection();
            // 启动连接
            connection.start();
            // 创建一个session会话
            session = connection.createQueueSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			LOGGER.error("connection amq error.", e);
		}
	}

	public void closeActiveMQ() {
		// 关闭释放资源
		try {
			if (session != null) {
				session.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
