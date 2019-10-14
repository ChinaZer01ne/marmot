package com.github.marmot.protocol;

/**
 * @author Zer01ne
 * @date 2019/3/22 10:11
 * @version 1.0
 */
public enum ProtocolType {
    /**
     * 服务端、客户端初始化
     * */
    INIT(1),
    INIT_STATUS(2),
    /**
     * Http连接请求建立
     * */
    CONNECTED(3),
    DISCONNECTED(4),
    /**
     * Http数据传输
     * */
    DATA(5),
    /**
     * 心跳包
     * */
    HEARTBEATS(6);

    private int code;

    ProtocolType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProtocolType valueOf(int code) throws Exception {
        for (ProtocolType item : ProtocolType.values()) {
            if (item.code == code) {
                return item;
            }
        }
        throw new Exception("ProtocolType code error: " + code);
    }
}
