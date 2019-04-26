package com.github.marmot.server.handler;
import com.github.marmot.protocol.ProtocolType;
import com.github.marmot.protocol.NetProtocol;
import com.github.marmot.server.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Map;
import java.util.Objects;
/**
 * 内网客户端连接的端口处理器
 * @author Zer01ne
 * @since 2019/4/1 17:54
 * @version 1.0
 */
public class ServerForwardHandler extends SimpleChannelInboundHandler<NetProtocol> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, NetProtocol protocol) throws Exception {

        ProtocolType type = protocol.getType();
        if (type == ProtocolType.INIT){
            System.err.println("客户端服务端连接建立中。。。");
            //内网客户端启动，发起初始化连接操作
            initChannel(channelHandlerContext,protocol);
        } else if (type == ProtocolType.DATA){
            System.err.println("接收到内网响应");
            Map<String, Channel> channelMap = ProxyPortHandler.CHANNEL_MAP;
            for (Map.Entry<String, Channel> entry:
                    channelMap.entrySet()) {
                if (Objects.equals(entry.getKey(),protocol.getChannelId())){
                    Channel channel = entry.getValue();
                    channel.writeAndFlush(protocol.getData());
                    break;
                }

            }

        }

    }

    private void initChannel(ChannelHandlerContext channelHandlerContext, NetProtocol protocol) throws InterruptedException {


        //保存客户端管道
        Channel clientChannel = channelHandlerContext.channel();

        System.err.println("客户端管道已注册");

        //一旦内网客户端启动，连接后，公网转发端口就要映射一个公网可以访问的端口
        final ChannelHandlerContext ctx = channelHandlerContext;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(channelHandlerContext.channel().eventLoop().parent(),channelHandlerContext.channel().eventLoop()).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                //传入公网转发端口的处理器，作用是，当映射端口接收当请求时，直接往该处理器里写数据
                pipeline.addLast(new ByteArrayEncoder());
                pipeline.addLast(new ProxyPortHandler(ctx));

            }
        });

        //公网映射访问端口
        Map<String, String> userConfig = ServerConfig.getUserConfig();
        int requestPort = Integer.parseInt(userConfig.get("requestPort"));

        serverBootstrap.bind(requestPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()){
                    System.err.println("外网请求端口已开启，监听：10000");

                    //返回客户端注册成功，然后客户端需要连接内网程序访问端口了
                    NetProtocol registerStatus = new NetProtocol();
                    registerStatus.setType(ProtocolType.INIT_STATUS);
                    registerStatus.setChannelId(clientChannel.id().asLongText());
                    registerStatus.setData("success".getBytes());
                    clientChannel.writeAndFlush(registerStatus);
                    System.err.println("客户端链接成功");

                }
            }
        });

    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()){
                case READER_IDLE:
                    System.out.println("读空闲");
                    break;
                case WRITER_IDLE:
                    System.out.println("写空闲");
                    break;
                case ALL_IDLE:
                    System.out.println("读写空闲");
                    break;
                default:
                    break;
            }
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
