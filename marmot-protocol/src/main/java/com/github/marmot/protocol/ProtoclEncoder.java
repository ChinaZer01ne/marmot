package com.github.marmot.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义协议编码器
 * @author Zer01ne
 * @date 2019/3/22 10:15
 * @version 1.0
 *
 * 自定义协议：
 *      报文 = 报文总长度 + Type + CHANNEL_ID长度 + CHANNEL_ID + DATA长度 + DATA
 */
public class ProtoclEncoder extends MessageToByteEncoder<NetProtocol> {

    private static final int TYPE_SIZE = 4;
    private static final int CHANNEL_ID_SIZE = 4;
    private static final int DATA_LENGTH_SIZE = 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, NetProtocol msg, ByteBuf out) throws Exception {
        //messageLength记录了总长度
        int messageLength = TYPE_SIZE + CHANNEL_ID_SIZE;

        byte[] data = msg.getData();
        if (data != null){
            int dataLength = data.length;
            messageLength += DATA_LENGTH_SIZE;
            messageLength += dataLength;
        }


        String channelId = msg.getChannelId();
        if (msg.getChannelId() != null){
            byte[] channelIdBytes = channelId.getBytes();
            messageLength += channelIdBytes.length;

        }

        out.writeInt(messageLength);

        int type = msg.getType().getCode();
        out.writeInt(type);

        if (msg.getChannelId() != null){
            byte[] channelIdBytes = channelId.getBytes();
            out.writeInt(channelIdBytes.length);
            out.writeBytes(channelIdBytes);
        }


        if (data != null){
            out.writeInt(data.length);
            out.writeBytes(data);
        }

    }
}
