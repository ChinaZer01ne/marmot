package com.github.marmot.server.access;

import com.github.marmot.access.ConfigCollector;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 从配置文件中加载配置
 * @author Zer01ne
 * @version 1.0
 * @date 2019/10/12 16:25
 */
public class ServerConfigFileLoader implements ConfigCollector {
    @Override
    public Map<String, String> readUserAttr() throws IOException {
        //readByNio();
        //read();
        return read();
    }

    private Map<String, String> read() {

        Map<String, String> config = new HashMap<>(8);

        File configFile = new File("C:\\Project\\marmot\\marmot-server\\src\\main\\java\\com\\github\\marmot\\server\\config\\marmot.conf");
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
            throw new RuntimeException("Could not find marmot.conf file or no permission!");
        }



        return Objects.equals(config.get("Switch"),"on") ? config : null;
    }


    private void readByNio() throws IOException {
        File configFile = new File("./marmot.conf");
        //Path path = Paths.get("./marmot.conf");
        //Files.walkFileTree();
        //InputStream inputStream = Files.newInputStream(path);
        FileInputStream fileInputStream = new FileInputStream(configFile);
        FileChannel channel = fileInputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);

        //configFile
        //FileChannel fileChannel = Files.
    }
}