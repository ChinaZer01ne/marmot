package com.github.marmot.client.handler;


import com.alibaba.fastjson.JSONObject;
import com.github.marmot.client.config.ClientConfig;
import com.github.marmot.constant.MarnotConst;
import com.github.marmot.protocol.ProtocolType;
import com.github.marmot.protocol.NetProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接服务端的端口处理器
 * @author Zer01ne
 * @date 2019/3/22 11:04
 * @version 1.0
 */
public class ClientForwardHandler extends SimpleChannelInboundHandler<NetProtocol> {
    /**
     * 保存到服务端的活跃时间
     */
    public static final Map<String, Long> SERVER_ACTIVE_TIME = new ConcurrentHashMap<>();
    /**
     * 保存到服务端的通道
     */
    private static final Map<String,Channel> SERVER_CHANNEL_MAP = new ConcurrentHashMap<>();
    /** 保存本地连接（到web程序的channel） */
    private  Map<String,Channel> localConnectMap = new ConcurrentHashMap<>();
    /** 用户客户端配置*/
    private static final Map<String, String> config = ClientConfig.getUserConfig();
    /** 重试次数 */
    private int retryCount = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NetProtocol msg) throws Exception {

        if (msg.getType() == ProtocolType.INIT_STATUS){
            //处理初始化结果
            HandleInitResult(ctx, msg);

        } else if (msg.getType() == ProtocolType.CONNECTED){
            //连接建立需要打开内网转发端口到程序端口的通道
            buildConnect(ctx,msg);
            System.err.println("内网Http链接通道正在建立。。。");

        } else if (msg.getType() == ProtocolType.DATA){

            System.out.println("客户端接收到用户请求！");
            //处理用户请求
            handleUserRequest(msg);


        }else if(msg.getType() == ProtocolType.DISCONNECTED){

            System.err.println("客户端失去连接！");

            //过期的链接需要移除掉
            Channel expiredChannel = localConnectMap.remove(msg.getChannelId());
            if (expiredChannel != null){
                expiredChannel.close();
            }
        }else if(msg.getType() == ProtocolType.HEARTBEATS){
            pong(ctx, msg);
        }

    }
    /**
     * 心跳响应
     */
    private void pong(ChannelHandlerContext ctx, NetProtocol heartPack) {
        byte[] data = heartPack.getData();
        Map<String,String> heartbeats = JSONObject.parseObject(data, Map.class);
        if ("PING".equals(heartbeats.get("HeartType"))){
            System.out.println("客户端收到Ping心跳包，PONG~");
            NetProtocol pongPack = new NetProtocol();
            pongPack.setType(ProtocolType.HEARTBEATS);
            pongPack.setChannelId(ctx.channel().id().asLongText());
            Map<String,String> pongStatus = new HashMap<>(5);
            pongStatus.put("HeartType","PONG");
            pongStatus.put("ActiveTime", String.valueOf(System.currentTimeMillis()));
            pongPack.setData(JSONObject.toJSONBytes(pongStatus));
            ctx.writeAndFlush(pongPack);
        }else {
            System.out.println("客户端收到PONG心跳包");
            SERVER_ACTIVE_TIME.put(ctx.channel().id().asLongText(), Long.parseLong(heartbeats.get("ActiveTime")));
        }
    }


    /** 处理初始化结果 */
    private void HandleInitResult(ChannelHandlerContext ctx, NetProtocol msg) {
        System.err.println("客户端链接中");
        byte[] data = msg.getData();
        String status = new String(data, CharsetUtil.UTF_8);
        //如果失败则重试
        if (MarnotConst.FAIL.equals(status)){
            initRetry(ctx);
        }else if (MarnotConst.SUCCESS.equals(status)){
            //TODO 成功应该做什么呢？
            System.out.println("客户端链接成功");
            SERVER_CHANNEL_MAP.put(ctx.channel().id().asLongText(),ctx.channel());
        }
    }


    /** 初始化失败重试 */
    private void initRetry(ChannelHandlerContext ctx) {
        //重试或者关闭所有链接

        if (retryCount < ClientConfig.FAIL_RETRY_COUNT){

            NetProtocol protocol = new NetProtocol();
            protocol.setType(ProtocolType.INIT);
            protocol.setChannelId(ctx.channel().id().asLongText());
            String configJson = JSONObject.toJSONString(config);
            protocol.setData(configJson.getBytes());
            System.out.println("客户端通道已激活");
            ctx.channel().writeAndFlush(protocol);
            retryCount++;
        }else {
            System.out.println("链接服务端失败");
            ctx.channel().eventLoop().shutdownGracefully();
            ctx.channel().eventLoop().parent().shutdownGracefully();
        }

        System.out.println("客户端链接失败");
    }

    /**
     * Http的CONNECTED请求发起时，建立内网通向程序的通道
     */
    private void buildConnect(ChannelHandlerContext ctx, NetProtocol msg) throws InterruptedException {

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

        //用户配置本机地址
        int requestPort = Integer.parseInt(config.get("LocalProgramPort"));
        bootstrap.connect(new InetSocketAddress("localhost", requestPort)).addListener((ChannelFutureListener) (future) -> {
            if (future.isSuccess()) {
                // TODO 有可能没有连接成功，数据就过来了
                localConnectMap.put(msg.getChannelId(),future.channel());
                System.out.println("已连接程序访问端口 -> " + requestPort);
            } else {
                localConnectMap.remove(msg.getChannelId());
                future.channel().close();
                System.out.println("连接程序访问端口失败");
            }
        });

    }

    /** 处理用户请求 */
    private void handleUserRequest(NetProtocol msg) {
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
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NetProtocol protocol = new NetProtocol();
        protocol.setType(ProtocolType.INIT);
        protocol.setChannelId(ctx.channel().id().asLongText());

        // 增加认证功能
        String configJson = JSONObject.toJSONString(config);
        protocol.setData(configJson.getBytes());

        System.out.println("客户端通道已激活");
        ctx.channel().writeAndFlush(protocol);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()){
                case READER_IDLE:
                    System.out.println("读空闲");
                    NetProtocol pingPack = new NetProtocol();
                    pingPack.setChannelId(ctx.channel().id().asLongText());
                    pingPack.setType(ProtocolType.HEARTBEATS);
                    Map<String,String> heatbeats = new HashMap<>(3);
                    heatbeats.put("HeartType","PING");
                    pingPack.setData(JSONObject.toJSONBytes(heatbeats));
                    //pingPack.setData("PING".getBytes());
                    // 向所有服务端发送PING包
                    if (SERVER_CHANNEL_MAP.containsKey(ctx.channel().id().asLongText())){
                        ctx.channel().writeAndFlush(pingPack);
                    }
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
}
