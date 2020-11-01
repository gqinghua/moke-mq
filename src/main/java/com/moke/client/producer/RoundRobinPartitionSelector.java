package com.moke.client.producer;

import com.moke.commons.Message;
import com.moke.commons.cluster.Partition;
import com.moke.commons.exception.SimpleMQClientException;
import com.moke.commons.utils.PositiveAtomicCounter;

import java.util.List;

/**
 * 轮询的分区选择器，默认使用此选择器
 * @author jianglinzou
 * @date 2019/3/11 下午1:20
 */
public class RoundRobinPartitionSelector extends AbstractPartitionSelector {

    private final PositiveAtomicCounter sets = new PositiveAtomicCounter();



    public Partition getPartition0(final String topic, final List<Partition> partitions, final Message message)
            throws SimpleMQClientException {
        try {
            return partitions.get(this.sets.incrementAndGet() % partitions.size());
        }
        catch (final Throwable t) {
            throw new SimpleMQClientException(t);
        }
    }
}
