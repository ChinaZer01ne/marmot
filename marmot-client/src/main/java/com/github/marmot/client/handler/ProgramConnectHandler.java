package com.github.marmot.client.handler;

import com.github.marmot.protocol.ProtocolType;
import com.github.marmot.protocol.NetProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 连接本地程序端口的处理器
 * @author Zer01ne
 * @date 2019/3/25 11:53
 * @version 1.0
 */
public class ProgramConnectHandler extends SimpleChannelInboundHandler<byte[]> {

    private ChannelHandlerContext context;
    private NetProtocol protocol;

    public ProgramConnectHandler(ChannelHandlerContext context, NetProtocol protocol) {
        this.context = context;
        this.protocol = protocol;
    }

    /**
     * 本地程序响应
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {

        NetProtocol netProtocol = new NetProtocol();
        netProtocol.setType(ProtocolType.DATA);
        netProtocol.setChannelId(protocol.getChannelId());
        netProtocol.setData(msg);

        context.channel().writeAndFlush(netProtocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
