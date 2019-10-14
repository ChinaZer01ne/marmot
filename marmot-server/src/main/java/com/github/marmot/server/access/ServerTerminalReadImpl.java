package com.github.marmot.server.access;

import com.github.marmot.access.ConfigCollector;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 服务端终端配置读取
 * @author Zer01ne
 * @version 1.0
 * @date 2019/4/25 13:38
 */
public class ServerTerminalReadImpl implements ConfigCollector {


    private static Map<String,String> configMap = new HashMap<>(8);

    /** 获取用户配置信息 */

    @Override
    public Map<String, String> readUserAttr() throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));


        bufferedWriter.write("内网映射访问端口：\n");
        bufferedWriter.flush();
        String accessPort = bufferedReader.readLine();
        while (accessPort.matches(PORT_PATTERN)){
            try {
                throw  new Exception("内网映射访问端口：\n");
            } catch (Exception e) {
                e.printStackTrace();
                bufferedWriter.write("内网映射访问端口：\n");
                bufferedWriter.flush();
                accessPort = bufferedReader.readLine();
            }
        }
        configMap.put("AccessPort",accessPort);

        bufferedWriter.write("请输入运行端口号：\n");
        bufferedWriter.flush();
        Optional<String> proxyPort = Optional.of(bufferedReader.readLine());
        String port = proxyPort.filter(s -> s.matches(PORT_PATTERN)).orElse(DEFAULT_PORT);
        configMap.put("ProxyPort",port);


        bufferedWriter.write("请输入用户名：\n");
        bufferedWriter.flush();
        Optional<String> username = Optional.of(bufferedReader.readLine());
        username.ifPresent(s -> configMap.put("User",s));

        bufferedWriter.write("请输入密码：\n");
        bufferedWriter.flush();
        Optional<String> password = Optional.of(bufferedReader.readLine());
        password.ifPresent(s -> configMap.put("Password",s));

        return Collections.unmodifiableMap(configMap);
    }

}
