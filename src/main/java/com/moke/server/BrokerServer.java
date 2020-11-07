package com.moke.server;


/**
 *
 * @author 92306
 */
public interface BrokerServer {


    void init();


    /**
     * 启动接口
     */
    void  start(int port) throws Exception;
}
