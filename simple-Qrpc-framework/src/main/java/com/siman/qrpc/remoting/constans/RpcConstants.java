package com.siman.qrpc.remoting.constans;

import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * @author SiMan
 * @date 2021/1/20 15:26
 */

public class RpcConstants {

    /**
     * 魔数. 用于校验 Rpc 消息
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'q', (byte) 'r', (byte) 'p', (byte) 'c'};

    public static final Charset DEFAULT_CHARSET = CharsetUtil.UTF_8;

    public static final byte VERSION = 1;

    public static final byte TOTAL_LENGTH = 16;

    public static final byte REQUEST_TYPE = 1;

    public static final byte RESPONSE_TYPE = 2;

    /**
     * 心跳请求
     */
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;

    /**
     * 心跳响应
     */
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final int HEAD_LENGTH = 16;

    public static final String PING = "ping";

    public static final String PONG = "pong";

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}