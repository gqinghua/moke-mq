package com.xs.test.producer;

import com.alibaba.fastjson.JSON;
import com.moke.mq.client.producer.Producer;
import com.moke.mq.client.server.ServerMap;
import com.moke.mq.common.Message;

public class ProducerTest {

    public static String ip="127.0.0.1";
    public static Integer port=9870;
    public static String topic="tp_test1";
    public static void main(String[] args) {

        ServerMap.addTopicServer(ip,port,topic);

        for(int i=0;i<100;i++){
            Message msg = new Message();
            msg.setTopic("tp_test1");
            StringBuffer sb = new StringBuffer();
            sb.append("[").append(i).append("] send msg soomq ");
            msg.setBody(sb.toString().getBytes());
            System.out.println(JSON.toJSONString(msg));
            Producer.sengMsg(msg);
        }

    }
}
