package com.github.marmot.server.config;

import com.github.marmot.access.ConfigCollector;
import com.github.marmot.server.access.ServerConfigFileLoader;
import com.github.marmot.server.access.ServerTerminalReadImpl;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Zer01ne
 * @version 1.0
 * @date 2019/4/25 13:24
 */
public class ServerConfig {

    /** 用户配置信息*/
    private static Map<String,String> configMap;

    private static ConfigCollector configCollector;

    /** 读取用户配置信息 */
    public static Map<String,String> readAttr() throws IOException {

        //configCollector = new ServerTerminalReadImpl();
        //configMap = configCollector.readUserAttr();
        configCollector = new ServerConfigFileLoader();
        configMap = configCollector.readUserAttr();
        if (configMap == null){
            configCollector = new ServerTerminalReadImpl();
            configMap = configCollector.readUserAttr();
        }
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
