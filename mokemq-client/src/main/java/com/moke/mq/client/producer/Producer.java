package com.moke.mq.client.producer;

import com.moke.mq.client.server.Server;
import com.moke.mq.client.server.ServerMap;
import com.moke.mq.common.Message;
import com.moke.mq.common.exception.SooMQException;

public class Producer {


    public static void sengMsg(Message msg){
        Server server = ServerMap.TopicServer.get(msg.getTopic());
        if(server==null){
            throw new SooMQException("this topic["+msg.getTopic()+"] have no server");
        }
        TopicProducerFactory.get(server.getIp(),server.getPort(),msg.getTopic()).sengMsg(msg);
    }


}
