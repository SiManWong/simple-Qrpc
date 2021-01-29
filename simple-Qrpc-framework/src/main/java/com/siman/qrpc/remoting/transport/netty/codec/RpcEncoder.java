package com.siman.qrpc.remoting.transport.netty.codec;

import com.siman.qrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author SiMan
 * @date 2021/1/21 0:32
 */
@AllArgsConstructor
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        if (genericClass.isInstance(msg)) {
            // 将对象转换为 byte
            byte[] bytes = serializer.serialize(msg);
            // 读取消息的长度
            int length = bytes.length;
            // 写入消息对应的字节数组长度
            out.writeInt(length);
            // 将字节数组写入 ByteBuf 中
            out.writeBytes(bytes);
            log.info("successful encode Object to ByteBuf");
        }
    }
}
