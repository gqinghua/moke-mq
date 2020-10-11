package cn.moke.mq.server;


import static  cn.moke.mq.util.ZkUtils.ZK_MQ_BASE;

import java.io.Closeable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.moke.mq.cluster.Cluster;
import cn.moke.mq.cluster.Group;
import cn.moke.mq.config.ServerConfig;
import cn.moke.mq.exception.ZkNodeExistsException;
import cn.moke.mq.util.DataUtils;
import cn.moke.mq.util.ZkUtils;
import cn.moke.mq.zk.ZkClient;

/**
 * Handles the server's interaction with zookeeper. The server needs to register the following
 * paths:
 * <p/>
 * <pre>
 *   /uncode/mq/brokergroup/
 * </pre>
 *
 * @author juny.ye
 */
public class ServerRegister implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegister.class);
    
    public static final String ZK_BROKER_GROUP = ZK_MQ_BASE + "/brokergroup";
    public static final String AMQ_PATH = "/activemq";

    private ServerConfig config;
    
    private ZkClient zkClient;

    public ZkClient startup(ServerConfig config) {
    	LOGGER.info("connecting to zookeeper: " + config.getZkConnect());
    	this.config = config;
    	if(zkClient == null){
    		String authString = config.getZkUsername() + ":"+ config.getZkPassword();
            zkClient = new ZkClient(config.getZkConnect(), authString, config.getZkSessionTimeoutMs(),
                    config.getZkConnectionTimeoutMs());
    	}
        registerBrokerGroupInZk();
        ZkUtils.getCluster(zkClient);
        return zkClient;
    }


    /**
     * register broker group in the zookeeper
     * <p>
     * path: /uncode/mq/brokergroup/<id> <br/>
     * data: json
     * </p>
     */
    public void registerBrokerGroupInZk() {
    	String zkPath = ZK_BROKER_GROUP;
    	ZkUtils.makeSurePersistentPathExists(zkClient, zkPath);
    	LOGGER.info("Registering broker group" + zkPath);
    	ZkUtils.makeSurePersistentPathExists(zkClient, ZK_MQ_BASE + AMQ_PATH);
    	if(StringUtils.isNotBlank(config.getActiveBrokerUrl())){
    		ZkUtils.makeSurePersistentPathExists(zkClient, ZK_MQ_BASE + AMQ_PATH + "/path");
    		zkClient.writeData(ZK_MQ_BASE + AMQ_PATH + "/path", config.getActiveBrokerUrl().getBytes());
    	}
        //
        Group brokerGroup = new Group(config.getBrokerGroupName(), config.getHost(), config.getPort(), config.getReplicaHost());
        zkPath += "/" + brokerGroup.getName();
        String jsonGroup = DataUtils.brokerGroup2Json(brokerGroup);
        try {
        	ZkUtils.getCluster(zkClient);
        	if(!Cluster.getMasterIps().contains(config.getHost())){
                ZkUtils.createEphemeralPathExpectConflict(zkClient, zkPath, jsonGroup);
                Cluster.setCurrent(brokerGroup);//暂存，index中使用
        	}
        } catch (ZkNodeExistsException e) {
            String oldServerInfo = ZkUtils.readDataMaybeNull(zkClient, zkPath);
            String message = "A broker (%s) is already registered on the path %s." //
                    + " This probably indicates that you either have configured a brokerid that is already in use, or "//
                    + "else you have shutdown this broker and restarted it faster than the zookeeper " ///
                    + "timeout so it appears to be re-registering.";
            message = String.format(message, oldServerInfo, zkPath);
            throw new RuntimeException(message);
        }
        //
        LOGGER.info("Registering broker group" + zkPath + " succeeded with " + jsonGroup);
    }

    /**
     *
     */
    public void close() {
        if (zkClient != null) {
        	LOGGER.info("closing zookeeper client...");
            zkClient.close();
        }
    }
    
}
