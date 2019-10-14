package com.github.marmot.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    String[] clientConfigFilePaths = new String[]{".\\marmot.conf","~\\marmot.conf","C:\\Project\\marmot\\marmot-client\\src\\main\\java\\com\\github\\marmot\\client\\config\\marmot.conf"};
    String[] serverConfigFilePaths = new String[]{".\\marmot.conf","~\\marmot.conf","C:\\Project\\marmot\\marmot-server\\src\\main\\java\\com\\github\\marmot\\server\\config\\marmot.conf"};

    /**读取用户的配置属性*/
    Map<String,String> readUserAttr() throws IOException;

    default Map<String,String> loadLocalConfig(){
        Map<String, String> config = new HashMap<>(8);
        // 扫描配置文件
        File configFile = null;

        String[] configFilePaths = serverConfigFilePaths;
        if (this.getClass().getName().contains("Client")){
            configFilePaths = clientConfigFilePaths;
        }

        for (String configFilePath : configFilePaths) {
            configFile = new File(configFilePath);
            if (configFile.exists()){
                break;
            }
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile))) {
            String configItem = null;
            while ((configItem = bufferedReader.readLine()) != null){
                // 读到注释或者空行的情况
                if (configItem.startsWith("#") || configItem.trim().length() == 0){
                    continue;
                }
                String[] configLine = configItem.trim().split(" ");
                if (configLine.length < 2){
                    throw new RuntimeException(String.format("No config defined, check marmot.conf near %s\n", configLine[configLine.length - 1]));
                }
                config.put(configLine[0],configLine[1]);
            }

        }catch (IOException e){
            System.out.println("Could not find marmot.conf file or no permission!");
            return null;
        }
        return Objects.equals(config.get("Switch"),"on") ? config : null;
    }
}
