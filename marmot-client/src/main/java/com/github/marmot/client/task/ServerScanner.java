package com.github.marmot.client.task;

import com.github.marmot.client.handler.ClientForwardHandler;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 扫描可能下线的服务端
 * @author Zer01ne
 * @version 1.0
 * @date 2019/10/12 16:25
 **/
public class ServerScanner implements Callable<Object> {

    private static final Map<String,Long> CLIENT_ACTIVE_TIME = ClientForwardHandler.SERVER_ACTIVE_TIME;
    private static final long OVER_TIME = 30 * 1000L;
    @Override
    public Object call() throws Exception {
        while (true){
            CLIENT_ACTIVE_TIME.forEach((channelId, time) -> {
                if (System.currentTimeMillis() - time > OVER_TIME){
                    System.out.println("服务端可能已下线，请检查网络" + channelId);
                    CLIENT_ACTIVE_TIME.remove(channelId);
                }
            });
            TimeUnit.SECONDS.sleep(5);
        }
    }
}