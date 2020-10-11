package cn.moke.mq.server.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.network.Backup;
import cn.moke.mq.network.Message;
import cn.moke.mq.network.Topic;
import cn.moke.mq.server.AbstractRequestHandler;
import cn.moke.mq.store.TopicQueue;
import cn.moke.mq.store.TopicQueuePool;
import cn.moke.mq.util.DataUtils;

/**
 * MQ生产者处理器
 *
 * @author : juny.ye
 */
public class ReplicaRequestHandler extends AbstractRequestHandler {
	
	public ReplicaRequestHandler(ServerConfig config) {
		super(config);
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(ReplicaRequestHandler.class);
	
	@Override
	public Message handler(Message request) {
		int fetchSize = config.getReplicaFetchSize();
		List<Topic> result = new ArrayList<Topic>();
		List<Backup> backups = (List<Backup>) DataUtils.deserialize(request.getBody());
		Set<String> queueList = TopicQueuePool.getQueueNameFromDisk();
		if(queueList != null && queueList.size() > 0){
			if(backups == null){
				backups = new ArrayList<Backup>();
			}
			for(Backup backup:backups){
				if(queueList.contains(backup.getQueueName())){
					queueList.remove(backup.getQueueName());
				}
			}
		}
		if(queueList != null && queueList.size() > 0){
			for(String queue:queueList){
				Backup backup = new Backup(queue, 0, 0, 0);
				backups.add(backup);
			}
		}
		if(backups != null){
			for(Backup backup:backups){
				TopicQueue queue = TopicQueuePool.getQueue(backup.getQueueName());
				if(null != queue){
					byte[] tpc = null;
					int rnum = backup.getSlaveWriteNum();
					int rposition = backup.getSlaveWritePosition();
					for(int i=0;i<fetchSize;i++){
						tpc = queue.replicaRead(rnum, rposition);
						if(null != tpc){
							rposition = rposition + tpc.length + 4;
							Topic tmp = (Topic) DataUtils.deserialize(tpc);
							result.add(tmp);
						}else{
							break;
						}
					}
				}
			}
		}
		Message response = Message.newResponseMessage();
		response.setSeqId(request.getSeqId());
		if(result.size() > 0){
			response.setBody(DataUtils.serialize(result));
			//LOGGER.info("Fetch request handler, message:"+result.toString());
		}
		
		return response;
	}


	

	
}
