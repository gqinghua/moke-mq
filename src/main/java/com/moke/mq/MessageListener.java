package com.moke.mq;

/**
 * Created by yang on 16-11-24.
 */
public abstract interface  MessageListener {
    public abstract ConsumeResult onMessage(Message paramMessage);
}
