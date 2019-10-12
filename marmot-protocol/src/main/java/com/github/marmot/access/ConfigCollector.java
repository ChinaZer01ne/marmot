package com.github.marmot.access;

import java.io.IOException;
import java.util.Map;

/**
 * 用户界面接入方式
 *
 * @author Zer01ne
 * @version 1.0
 * @date 2019/4/25 11:11
 */
public interface ConfigCollector {

    String DEFAULT_PORT = "8888";
    String IP_PORT_PATTERN = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d):[1-9][0-9]+";
    String PORT_PATTERN = "[1-9]+[0-9]+";

    /**读取用户的配置属性*/
    Map<String,String> readUserAttr() throws IOException;
}
