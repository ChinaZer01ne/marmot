package com.github.marmot.server;


import com.github.marmot.protocol.ProtoclDecoder;
import com.github.marmot.protocol.ProtoclEncoder;
import com.github.marmot.server.handler.ServerForwardHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
/**
 * @author Zer01ne
 * @since 2019/3/25 11:29
 * @version 1.0
 */
public class MarmotServer {
    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            /**
             * 监听外网转发端口
             */
            ServerBootstrap outerServerBootstrap = new ServerBootstrap();
            outerServerBootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    //解决粘包拆包问题，initialBytesToStrip跳过的字节是我们定义的数据总长度，该类通过总长度可以解决粘包拆包为题
                    pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast(new IdleStateHandler(5,7,3,TimeUnit.SECONDS));
                    pipeline.addLast(new ProtoclDecoder());
                    pipeline.addLast(new ProtoclEncoder());
                    pipeline.addLast(new ServerForwardHandler());
                }
            });
            ChannelFuture outerChannelFuture = outerServerBootstrap.bind(8888).sync();
            System.err.println("外网转发端口已开启，监听：8888");

            outerChannelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
