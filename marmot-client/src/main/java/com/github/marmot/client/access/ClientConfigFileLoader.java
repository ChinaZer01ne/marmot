package com.github.marmot.client.access;


import com.github.marmot.access.ConfigCollector;

import java.io.IOException;
import java.util.Map;

/**
 * 从配置文件中加载配置
 * @author Zer01ne
 * @version 1.0
 * @date 2019/10/12 16:25
 */
public class ClientConfigFileLoader implements ConfigCollector {


    @Override
    public Map<String, String> readUserAttr() throws IOException {
        return null;
    }
}