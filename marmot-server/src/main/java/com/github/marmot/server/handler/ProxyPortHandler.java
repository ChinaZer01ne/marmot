package com.github.marmot.server.handler;


import com.github.marmot.protocol.ProtocolType;
import com.github.marmot.protocol.NetProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公网访问的代理端口处理器
 * @author Zer01ne
 * @date  2019/3/25 11:29
 * @version  1.0
 */
public class ProxyPortHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final Map<String,Channel> CHANNEL_MAP = new ConcurrentHashMap<>();
    private ChannelHandlerContext context;

    public ProxyPortHandler(ChannelHandlerContext context) {
        this.context = context;
    }


    /**
     * 接收到网络Http请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        System.err.println("接收到网络请求");
        byte[] data = ByteBufUtil.getBytes(msg);
        NetProtocol protocol = new NetProtocol();
        protocol.setType(ProtocolType.DATA);
        protocol.setData(data);
        protocol.setChannelId(ctx.channel().id().asLongText());
        context.channel().writeAndFlush(protocol);
    }
    /**
     * 网络发起Http请求时，通知内网建立到程序访问端口的通道
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        CHANNEL_MAP.put(ctx.channel().id().asLongText(),ctx.channel());
        //接收到Http请求后，建立内网转发端口到程序的通道
        NetProtocol protocol = new NetProtocol();
        protocol.setChannelId(ctx.channel().id().asLongText());
        protocol.setType(ProtocolType.CONNECTED);
        context.channel().writeAndFlush(protocol);
    }
    /**
     * 通知内网关闭通道
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        //断开Http链接，销毁内网转发端口到程序的通道
        NetProtocol protocol = new NetProtocol();
        protocol.setChannelId(ctx.channel().id().asLongText());
        protocol.setType(ProtocolType.DISCONNECTED);
        context.channel().writeAndFlush(protocol);
        CHANNEL_MAP.remove(ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

}
