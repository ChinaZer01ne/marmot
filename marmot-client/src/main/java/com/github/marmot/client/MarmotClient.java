package com.github.marmot.client;

import com.github.marmot.client.handler.ClientForwardHandler;
import com.github.marmot.protocol.ProtoclDecoder;
import com.github.marmot.protocol.ProtoclEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;

/**
 * @author Zer01ne
 * @date 2019/3/11 9:43
 * @version 1.0
 */
public class MarmotClient {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast(new ProtoclDecoder());
                    pipeline.addLast(new ProtoclEncoder());
                    pipeline.addLast(new ClientForwardHandler());
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(8888)).sync();
            System.err.println("已连接服务器转发端口：8888");
            channelFuture.channel().closeFuture().sync();
        }finally {
            workerGroup.shutdownGracefully();
        }
    }
}
