package com.moke.server;

import broker.netty.broker.ClientChannelInfo;
import broker.netty.model.StormRequest;
import broker.netty.model.SubscriptRequestinfo;
import broker.netty.smq.ConsumeResult;
import broker.netty.smq.Message;
import io.netty.channel.Channel;

/**
 * Created by yang on 16-11-24.
 */
public abstract class MessageListener {
   //当收到来自producer的消息时触发的事件,处理完成后需要返回一个ACK确认信息
    void onProducerMessageReceived(Message msg, String requestId, Channel channel){
    }
    //当收到consumer发送的消费信息的事件
    void onConsumerResultReceived(ConsumeResult msg){

    }
    //当收到客户端的订阅信息触发的事件
    void onConsumerSubcriptReceived(SubscriptRequestinfo msg, ClientChannelInfo  channel){

    }
    //当收到请求触发的事件
    void onRequest(StormRequest request){

    }
    //当异常时触发的事件
    void onError(Throwable t){

    }
}
