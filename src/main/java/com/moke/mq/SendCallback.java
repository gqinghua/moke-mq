package com.moke.mq;

/**
 * Created by yang on 16-11-22.
 */
public abstract interface SendCallback {
    public abstract void onResult(SendResult papramSendResult);
}
