package com.github.marmot.client.handler;


import com.alibaba.fastjson.JSONObject;
import com.github.marmot.client.config.ClientConfig;
import com.github.marmot.protocol.ProtocolType;
import com.github.marmot.protocol.NetProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接服务端的端口处理器
 * @author Zer01ne
 * @date 2019/3/22 11:04
 * @version 1.0
 */
public class ClientForwardHandler extends SimpleChannelInboundHandler<NetProtocol> {

    private  Map<String,Channel> localConnectMap = new ConcurrentHashMap<>();
    private static final Map<String, String> config = ClientConfig.getUserConfig();
    private int retryCount = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetProtocol msg) throws Exception {



        if (msg.getType() == ProtocolType.INIT_STATUS){
            System.err.println("客户端链接中");
            byte[] data = msg.getData();
            String status = new String(data, CharsetUtil.UTF_8);

            if ("fail".equals(status)){
                /**
                 * 重试或者关闭所有链接   TODO
                 */

                if (retryCount < 3){

                    NetProtocol protocol = new NetProtocol();
                    protocol.setType(ProtocolType.INIT);
                    protocol.setChannelId(ctx.channel().id().asLongText());
                    //Map<String, String> config = ClientConfig.getUserConfig();
                    String configJson = JSONObject.toJSONString(config);
                    protocol.setData(configJson.getBytes());
                    System.out.println("客户端通道已激活");
                    ctx.channel().writeAndFlush(protocol);
                    retryCount++;
                }else {
                    System.out.println("链接服务端失败");
                    //ctx.executor().shutdownGracefully();
                    //ctx.executor().parent().shutdownGracefully();
                    ctx.channel().eventLoop().shutdownGracefully();
                    ctx.channel().eventLoop().parent().shutdownGracefully();
                }

                System.out.println("客户端链接失败");
            }else if ("success".equals(status)){
                System.out.println("客户端链接成功");
            }

        } else if (msg.getType() == ProtocolType.CONNECTED){
            //连接建立需要打开内网转发端口到程序端口的通道
            connectSuccess(ctx,msg);
            System.err.println("内网Http链接通道正在建立。。。");

        } else if (msg.getType() == ProtocolType.DATA){

            System.out.println("客户端接收到用户请求！");
            //有可能连接还没放到map中，数据就过来了,等待连接建立
            if (localConnectMap.get(msg.getChannelId()) != null){

                localConnectMap.get(msg.getChannelId()).writeAndFlush(msg.getData());

            }else {
                System.out.println("连接尚未建立！");
                // 这里使用线程池，采用任务队列应该会更好，不过正常使用应该没有多少队列产生
                ClientConfig.threadPool.execute(() -> {
                    while (true){
                        if (localConnectMap.get(msg.getChannelId()) != null){
                            localConnectMap.get(msg.getChannelId()).writeAndFlush(msg.getData());
                            break;
                        }
                    }
                });
            }


        }else if(msg.getType() == ProtocolType.DISCONNECTED){

            System.err.println("客户端失去连接！");

            //过期的链接需要移除掉
            Channel expiredChannel = localConnectMap.remove(msg.getChannelId());
            if (expiredChannel != null){
                expiredChannel.close();
            }
        }

    }
    /**
     * Http的CONNECTED请求发起时，建立内网通向程序的通道
     */
    private void connectSuccess(ChannelHandlerContext ctx, NetProtocol msg) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();

                pipeline.addLast(new ByteArrayDecoder());
                pipeline.addLast(new ByteArrayEncoder());
                pipeline.addLast(new ProgramConnectHandler(ctx,msg));

            }
        });

        bootstrap.connect(new InetSocketAddress("localhost", 9090)).addListener((ChannelFutureListener) (future) -> {
            if (future.isSuccess()) {
                // TODO 有可能没有连接成功，数据就过来了
                localConnectMap.put(msg.getChannelId(),future.channel());
                System.out.println("已连接程序访问端口：9090");
            } else {
                localConnectMap.remove(msg.getChannelId());
                future.channel().close();
                System.out.println("连接程序访问端口失败");
            }
        });

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NetProtocol protocol = new NetProtocol();
        protocol.setType(ProtocolType.INIT);
        protocol.setChannelId(ctx.channel().id().asLongText());
        System.out.println("客户端通道已激活");
        ctx.channel().writeAndFlush(protocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
