package com.moke.server.SendResult;

/**
 *消息发送的结果.
 *
 * @author 92306
 */
public class SendResult {
    private String info;
    private SendStatus status;
    private String msgId;

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public SendStatus getStatus() {
        return status;
    }

    public void setStatus(SendStatus status) {
        this.status = status;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    @Override
    public String toString(){
        return new StringBuilder().append("msg ").append(this.msgId).append("  send ").append(this.status == SendStatus.SUCCESS ? "success":"fail").append("   info:").append(this.info).toString();
    }


}
