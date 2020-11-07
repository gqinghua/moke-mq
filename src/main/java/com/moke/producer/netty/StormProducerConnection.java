package com.moke.producer.netty;


import com.moke.model.InvokeFuture;
import com.moke.model.MokeRequest;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * producer和broker之间的连接.
 */
public interface StormProducerConnection {
    void init();
    void connect();
    void connect(String host, int port);
    void setHandler(ChannelInboundHandlerAdapter handler);
    Object Send(MokeRequest request);
    void Send(MokeRequest request, final SendCallback listener);
    void close();
    boolean isConnected();
    boolean isClosed();
    public boolean ContrainsFuture(String key);
    public InvokeFuture<Object> removeFuture(String key);
    public void setTimeOut(long timeOut);

}
