package com.github.marmot.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * 自定义协议解码器
 * @author Zer01ne
 * @date 2019/3/22 10:15
 * @version 1.0
 */
public class ProtoclDecoder extends ByteToMessageDecoder {

    private static final int INT_SIZE = 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int type = in.readInt();

        byte[] channel = null;
        if (in.readableBytes() > INT_SIZE){
            int channelLength = in.readInt();
            channel = new byte[channelLength];
            //if (in.isReadable()) {
                in.readBytes(channel);
            //}

        }
        byte[] data = null;
        if (in.readableBytes() > INT_SIZE){

            int dataLength = in.readInt();
            data = new byte[dataLength];
            //if (in.isReadable()) {
                in.readBytes(data);
            //}
        }



        NetProtocol protocol = new NetProtocol();
        protocol.setType(ProtocolType.valueOf(type));
        protocol.setChannelId(channel != null? new String(channel,Charset.forName("utf-8")) : "");
        protocol.setData(data);

        out.add(protocol);
    }
}
