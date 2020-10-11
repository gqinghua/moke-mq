package cn.moke.mq.server.handlers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.network.Message;
import cn.moke.mq.network.Topic;
import cn.moke.mq.server.AbstractRequestHandler;
import cn.moke.mq.store.TopicQueue;
import cn.moke.mq.store.TopicQueuePool;
import cn.moke.mq.util.DataUtils;

/**
 * MQ消费处理器
 *
 * @author : juny.ye
 */
public class FetchRequestHandler extends AbstractRequestHandler {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(FetchRequestHandler.class);
	
	public FetchRequestHandler(ServerConfig config) {
		super(config);
	}
	
	@Override
	public Message handler(Message request) {
		List<Topic> result = new ArrayList<Topic>();
		List<Topic> topics = (List<Topic>) DataUtils.deserialize(request.getBody());
		if(topics != null){
			for(Topic topic:topics){
				byte[] tpc = null;
				TopicQueue queue = TopicQueuePool.getQueue(topic.getTopic());
				if(null != queue){
					for(int i=0;i<config.getFetchSize();i++){
						tpc = queue.poll();
						if(null != tpc){
							Topic tmp = (Topic) DataUtils.deserialize(tpc);
							tmp.setReadCounter(queue.getIndex().getReadCounter());
							tmp.setWriteCounter(queue.getIndex().getWriteCounter());
							result.add(tmp);
						}
					}
				}
			}
		}
		Message response = Message.newResponseMessage();
		response.setSeqId(request.getSeqId());
		if(result.size() > 0){
			response.setBody(DataUtils.serialize(result));
			for(Topic tp:result){
//				LOGGER.info("Fetch request handler, message topic:"+tp.getTopic() + " read counter:" + tp.getReadCounter() + " write counter:" + tp.getWriteCounter() + ""+tp.toString());
				LOGGER.info("Fetch request handler, message:"+tp.toString());
			}
		}
		return response;
	}


	

	
}
