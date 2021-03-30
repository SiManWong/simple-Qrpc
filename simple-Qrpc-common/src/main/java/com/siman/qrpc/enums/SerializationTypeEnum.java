package com.siman.qrpc.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化方式枚举
 *
 * @author SiMan
 * @date 2021/1/20 15:17
 */
@Getter
@AllArgsConstructor
public enum SerializationTypeEnum {
    // kryo
    KRYO((byte) 0x01, "kryo"),
    // protostuff
    PROTOSTUFF((byte) 0x02, "protostuff");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
