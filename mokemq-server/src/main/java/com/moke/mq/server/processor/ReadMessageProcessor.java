package com.moke.mq.server.processor;

import com.moke.mq.common.Message;
import com.moke.mq.common.ProcessorCommand;
import com.moke.mq.server.message.MessageFile;
import com.moke.mq.server.message.MessageFileFactory;

public class ReadMessageProcessor implements Processor<Message,Message> {

    @Override
    public ProcessorCommand handle(ProcessorCommand task) {
        Message msg = task.getResult();
        MessageFile file = MessageFileFactory.getTopicFile(msg.getTopic());
        task = file.readMsg(task);
        return task;
    }


}
