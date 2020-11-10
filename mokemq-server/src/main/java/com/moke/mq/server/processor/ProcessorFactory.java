package com.moke.mq.server.processor;

import com.moke.mq.common.ProcessorType;

import java.util.HashMap;

public class ProcessorFactory {
    private static HashMap<String,Processor> processorClub;

    static {
        processorClub = new HashMap<>();
        processorClub.put(ProcessorType.SendMessage.getType(),new SendMessageProcessor());
        processorClub.put(ProcessorType.ReadMessage.getType(),new ReadMessageProcessor());

    }

    public synchronized static Processor getProcessorInstantiate(String type){
        Processor result=null;
        if(processorClub.containsKey(type)){
            result = processorClub.get(type);
        }
        return result;
    }

}
