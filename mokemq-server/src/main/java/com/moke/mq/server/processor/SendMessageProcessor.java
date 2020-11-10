package com.moke.mq.server.processor;

import com.moke.mq.common.Message;
import com.moke.mq.common.ProcessorCommand;
import com.moke.mq.server.message.MessageFile;
import com.moke.mq.server.message.MessageFileFactory;

public class SendMessageProcessor implements Processor<Message,Message> {

    @Override
    public ProcessorCommand handle(ProcessorCommand task) {
        MessageFile file = MessageFileFactory.getTopicFile(task.getResult().getTopic());
        task = file.appendMsg(task);
        return task;
    }


}
