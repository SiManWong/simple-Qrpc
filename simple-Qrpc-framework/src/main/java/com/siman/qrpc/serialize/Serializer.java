package com.siman.qrpc.serialize;

import com.siman.qrpc.extension.SPI;

/**
 * @author SiMan
 * @date 2021/1/21 0:33
 */
@SPI
public interface Serializer {

    /**
     * 序列化
     * @param obj 要序列化的对象
     * @return  字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 类
     * @param <T>
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
