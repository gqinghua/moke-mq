package com.moke.server.message;


import com.moke.broker.ClientChannelInfo;
import com.moke.model.MokeRequest;
import io.netty.channel.Channel;

/**
 * 消息队列基本通信接口
 */
public abstract class MessageListener {
    /**
     * 当收到来自producer的消息时触发的事件,处理完成后需要返回一个ACK确认信息
     */
    public void onProducerMessageReceived(Message msg, String requestId, Channel channel) {
    }

    /**
     * //当收到consumer发送的消费信息的事件
     *
     * @param msg
     */
    public void onConsumerResultReceived(ConsumeResult msg) {

    }

    /**
     * //当收到客户端的订阅信息触发的事件
     *
     * @param msg
     * @param channel
     */
    public void onConsumerSubcriptReceived(SubscriptRequestinfo msg, ClientChannelInfo channel) {

    }

    /**
     * //当收到请求触发的事件
     *
     * @param request
     */
    public void onRequest(MokeRequest request) {

    }

    /**
     * //当异常时触发的事件
     *
     * @param
     */
    public void onError(Throwable throwable) {

    }
}
