package cn.moke.mq.client;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.client.consumer.Consumer;
import cn.moke.mq.client.consumer.ConsumerSubscriber;
import cn.moke.mq.network.Topic;
import cn.moke.mq.util.JsonUtils;

public class OptionMessageListener implements MessageListener {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(OptionMessageListener.class);

	@Override
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
	
	

}
