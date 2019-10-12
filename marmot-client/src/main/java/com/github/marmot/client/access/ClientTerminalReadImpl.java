package com.github.marmot.client.access;

import com.github.marmot.access.ConfigCollector;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 客户端终端配置读取
 *
 * @author Zer01ne
 * @version 1.0
 * @date 2019/4/25 11:15
 */
public class ClientTerminalReadImpl implements ConfigCollector {

    private static Map<String,String> configMap = new HashMap<>(8);

    /** 获取用户配置信息 */

    @Override
    public Map<String, String> readUserAttr() throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));

        bufferedWriter.write("请输入远程主机映射地址：\n");
        bufferedWriter.flush();
        Optional<String> ipAndPort = Optional.of(bufferedReader.readLine());
        while (!ipAndPort.get().matches(IP_PORT_PATTERN)){
            try {
                throw new Exception("请指定远程主机映射地址：\n");
            } catch (Exception e) {
                e.printStackTrace();
                bufferedWriter.write("请输入远程主机映射地址：\n");
                bufferedWriter.flush();
                ipAndPort = Optional.of(bufferedReader.readLine());
            }
        }
        ipAndPort.ifPresent(s -> configMap.put("ipAndPort",s));

        bufferedWriter.write("请输入本地程序访问端口号：\n");
        bufferedWriter.flush();
        Optional<String>  requestPort = Optional.of(bufferedReader.readLine());
        while (!requestPort.isPresent() || !requestPort.get().matches(PORT_PATTERN)){
            try {
                throw  new Exception("请输入本地程序访问端口号：\n");
            } catch (Exception e) {
                e.printStackTrace();
                bufferedWriter.write("请输入本地程序访问端口号：\n");
                bufferedWriter.flush();
                requestPort = Optional.of(bufferedReader.readLine());
            }
        }
        requestPort.ifPresent(s -> configMap.put("requestPort",s));


        bufferedWriter.write("请输入用户名：\n");
        bufferedWriter.flush();
        Optional<String> username = Optional.of(bufferedReader.readLine());
        username.ifPresent(s -> configMap.put("username",s));

        bufferedWriter.write("请输入密码：\n");
        bufferedWriter.flush();
        Optional<String> password = Optional.of(bufferedReader.readLine());
        password.ifPresent(s -> configMap.put("password",s));

        return Collections.unmodifiableMap(configMap);
    }



}
