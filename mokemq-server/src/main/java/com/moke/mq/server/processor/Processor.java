package com.moke.mq.server.processor;

import com.moke.mq.common.ProcessorCommand;

public interface Processor<P,V> {
    public ProcessorCommand handle(ProcessorCommand task);
}
