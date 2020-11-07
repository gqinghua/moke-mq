package com.moke.server.message;


import com.moke.broker.ClientChannelInfo;
import io.netty.channel.Channel;

/**
 * 消息队列基本通信接口
 */
public abstract class MessageListener {
    /**
     * 当收到来自producer的消息时触发的事件,处理完成后需要返回一个ACK确认信息
     */
    void onProducerMessageReceived(Message msg, String requestId, Channel channel) {
    }

    /**
     * //当收到consumer发送的消费信息的事件
     *
     * @param msg
     */
    void onConsumerResultReceived(ConsumeResult msg) {

    }

    /**
     * //当收到客户端的订阅信息触发的事件
     *
     * @param msg
     * @param channel
     */
    void onConsumerSubcriptReceived(SubscriptRequestinfo msg, ClientChannelInfo channel) {

    }

    /**
     * //当收到请求触发的事件
     *
     * @param request
     */
    void onRequest(StormRequest request) {

    }

    /**
     * //当异常时触发的事件
     *
     * @param
     */
    void onError(Throwable throwable) {

    }
}
