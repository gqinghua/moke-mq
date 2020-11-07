package com.moke.server.message;


/**
 *  信道信息实体类
 * @author 92306 郭清华
 */
public class SubscriptRequestinfo {
    /**
     *  消费者属于哪个消费组.
     */
    private String groupId;

    /**
     *  //消费者要订阅的主题.
     */
    private String topic;
    /**
     * //订阅的过滤属性名
     */
    private String propertieName;
    /**
     * //订阅的过滤值
     */
    private String propertieValue;
    /**
     * //客户端的id.
     */
    private String clientKey;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPropertieName() {
        return propertieName;
    }

    public void setPropertieName(String propertieName) {
        this.propertieName = propertieName;
    }

    public String getPropertieValue() {
        return propertieValue;
    }

    public void setPropertieValue(String propertieValue) {
        this.propertieValue = propertieValue;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }
}
