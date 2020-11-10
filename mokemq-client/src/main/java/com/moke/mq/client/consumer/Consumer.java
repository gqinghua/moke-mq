package com.moke.mq.client.consumer;


import com.moke.mq.client.connection.ConnectionManager;
import com.moke.mq.client.server.Server;
import com.moke.mq.client.server.ServerMap;
import com.moke.mq.common.Message;
import com.moke.mq.common.ProcessorCommand;
import com.moke.mq.common.ProcessorType;
import com.moke.mq.common.exception.SooMQException;
import io.netty.channel.Channel;

public class Consumer {
    public static void readMsg(Message msg){
        try {
            Server server = ServerMap.TopicServer.get(msg.getTopic());
            if (server == null) {
                throw new SooMQException("this topic[" + msg.getTopic() + "] have no server");
            }
            Channel channel = ConnectionManager.get(server.getIp(), server.getPort());
            ProcessorCommand command = new ProcessorCommand();
            command.setResult(msg);
            command.setType(ProcessorType.ReadMessage.getType());
            channel.writeAndFlush(command);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
