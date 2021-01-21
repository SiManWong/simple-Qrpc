package com.siman.qrpc.enums;

/**
 * 序列化方式枚举
 *
 * @author SiMan
 * @date 2021/1/20 15:17
 */

public enum SerializationTypeEnum {
    // kyro
    KYRO((byte) 0x01, "kyro"),
    // protostuff
    PROTOSTUFF((byte) 0x02, "protostuff");

    private final byte code;
    private final String name;

    SerializationTypeEnum(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
