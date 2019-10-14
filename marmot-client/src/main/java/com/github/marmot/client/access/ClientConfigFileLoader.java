package com.github.marmot.client.access;


import com.github.marmot.access.ConfigCollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.sun.deploy.cache.Cache.exists;

/**
 * 从配置文件中加载配置
 * @author Zer01ne
 * @version 1.0
 * @date 2019/10/12 16:25
 */
public class ClientConfigFileLoader implements ConfigCollector {

    //private Path path = Paths.get(".\\marmot.conf","~\\marmot.conf","C:\\Project\\marmot\\marmot-client\\src\\main\\java\\com\\github\\marmot\\client\\config\\marmot.conf");
    private String[] configFilePaths = new String[]{".\\marmot.conf","~\\marmot.conf","C:\\Project\\marmot\\marmot-client\\src\\main\\java\\com\\github\\marmot\\client\\config\\marmot.conf"};
    @Override
    public Map<String, String> readUserAttr() throws IOException {
        return loadLocalConfig();
        //Map<String, String> config = new HashMap<>(8);
        //// 扫描配置文件
        //File configFile = null;
        ////for (Path pathItem : path) {
        ////    configFile = pathItem.toFile();
        ////    if (configFile.exists()){
        ////        break;
        ////    }
        ////}
        //
        //for (String configFilePath : configFilePaths) {
        //    configFile = new File(configFilePath);
        //    if (configFile.exists()){
        //        break;
        //    }
        //}
        //
        //try (BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile))) {
        //    String configItem = null;
        //    while ((configItem = bufferedReader.readLine()) != null){
        //        // 读到注释或者空行的情况
        //        if (configItem.startsWith("#") || configItem.trim().length() == 0){
        //            continue;
        //        }
        //        String[] configLine = configItem.trim().split(" ");
        //        if (configLine.length < 2){
        //            throw new RuntimeException(String.format("No config defined, check marmot.conf near %s\n", configLine[configLine.length - 1]));
        //        }
        //        config.put(configLine[0],configLine[1]);
        //    }
        //
        //}catch (IOException e){
        //    System.out.println("Could not find marmot.conf file or no permission!");
        //    return null;
        //}
        //
        //
        //
        //return Objects.equals(config.get("Switch"),"on") ? config : null;
    }
}