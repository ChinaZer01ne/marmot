package com.github.marmot.client.config;

import com.github.marmot.access.ConfigCollector;
import com.github.marmot.client.access.ClientTerminalReadImpl;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 客户端配置
 *
 * @author Zer01ne
 * @version 1.0
 * @date 2019/4/25 13:26
 */
public class ClientConfig {

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 1;
    private static final int KEEP_ALIVE_TIME = 60;
    /** 线程池配置 */
    public static ExecutorService threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),new DefaultThreadFactory("connect-build-thread-"));


    public static final int FAIL_RETRY_COUNT = 3;
    /** 用户配置信息*/
    private static Map<String,String> configMap;

    private static ConfigCollector configCollector;

    /** 读取用户配置信息 */
    public static Map<String,String> readAttr() throws IOException {

        configCollector = new ClientTerminalReadImpl();
        configMap = configCollector.readUserAttr();
        return configMap;
    }

    /** 获取用户配置信息 */
    public static Map<String,String> getUserConfig(){
        if (configMap != null){
            return configMap;
        }
        return Collections.emptyMap();
    }

}
