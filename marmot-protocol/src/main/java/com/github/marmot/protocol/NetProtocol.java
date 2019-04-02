package com.github.marmot.protocol;


/**
 * 自定义协议
 * @author Zer01ne
 * @date 2019/3/22 10:11
 * @version 1.0
 */
public class NetProtocol {

    private String channelId;
    private ProtocolType type;
    private byte[] data;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public ProtocolType getType() {
        return type;
    }

    public void setType(ProtocolType type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

