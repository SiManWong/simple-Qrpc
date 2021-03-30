package com.siman.qrpc.util.file;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * @author SiMan
 * @date 2021/2/3 2:06
 */
@Slf4j
public final class PropertiesFileUtil {
    private PropertiesFileUtil() {}

    public static Properties readPropertiesFile(String fileName){
        String rpcConfigPath = "";
        String rootPath = "";
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null) {
            try {
                rootPath = URLDecoder.decode(url.getPath(),"utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("occur exception when decode properties path [{}]", rootPath);
            }
            rpcConfigPath = rootPath + fileName;
        }
        Properties properties = null;
        try (FileInputStream fileInputStream = new FileInputStream(rpcConfigPath)){
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }

        return properties;
    }
}
