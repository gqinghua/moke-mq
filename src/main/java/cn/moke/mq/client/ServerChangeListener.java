package cn.moke.mq.client;

import java.util.List;

import cn.moke.mq.util.ZkUtils;
import cn.moke.mq.zk.ZkChildListener;
import cn.moke.mq.zk.ZkClient;

public class ServerChangeListener implements ZkChildListener{
	
	private final ZkClient zkClient;
	
	public ServerChangeListener(ZkClient zkClient){
		this.zkClient = zkClient;
	}

	@Override
	public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
		ZkUtils.getCluster(zkClient);
		
	}

	



}
