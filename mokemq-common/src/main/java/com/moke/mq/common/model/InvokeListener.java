package com.moke.mq.common.model;

/**
 * Created by yang on 16-11-22.
 */
public interface  InvokeListener<T> {
    void onResponse(T t);
}
