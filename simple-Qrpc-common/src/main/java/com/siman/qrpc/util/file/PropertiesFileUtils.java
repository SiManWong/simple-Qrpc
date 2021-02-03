package com.siman.qrpc.util.file;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author SiMan
 * @date 2021/2/3 2:06
 */
@Slf4j
public final class PropertiesFileUtils {
    private PropertiesFileUtils() {}

    public static Properties readPropertiesFile(String fileName){
        Properties properties = null;
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();

        try {
            rootPath = URLDecoder.decode(rootPath,"utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("occur exception when decode properties path [{}]", rootPath);
        }

        String rpcConfigPath = rootPath + fileName;
        try (FileInputStream fileInputStream = new FileInputStream(rpcConfigPath)){
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }

        return properties;
    }
}
