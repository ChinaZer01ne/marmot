package com.github.marmot.server.access;

import com.github.marmot.access.Input;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Zer01ne
 * @version 1.0
 * @date 2019/4/25 13:38
 */
public class ServerTeminalReadImpl implements Input {


    private static Map<String,String> configMap = new HashMap<>(8);

    /** 获取用户配置信息 */

    @Override
    public Map<String, String> readUserAttr() throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));


        bufferedWriter.write("内网映射访问端口：\n");
        bufferedWriter.flush();
        Optional<String> requestPort = Optional.of(bufferedReader.readLine());
        while (!requestPort.isPresent() || !requestPort.get().matches(PORT_PATTERN)){
            try {
                throw  new Exception("内网映射访问端口：\n");
            } catch (Exception e) {
                e.printStackTrace();
                bufferedWriter.write("内网映射访问端口：\n");
                bufferedWriter.flush();
                requestPort = Optional.of(bufferedReader.readLine());
            }
        }
        requestPort.ifPresent(s -> configMap.put("requestPort",s));

        bufferedWriter.write("请输入运行端口号：\n");
        bufferedWriter.flush();
        Optional<String> workPort = Optional.of(bufferedReader.readLine());
        String port = workPort.filter(s -> s.matches(PORT_PATTERN)).orElse(DEFAULT_PORT);
        configMap.put("workPort",port);


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
