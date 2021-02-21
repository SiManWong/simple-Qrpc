package com.siman.qrpc.remoting.transport.netty.codec;

import com.siman.qrpc.enums.SerializationTypeEnum;
import com.siman.qrpc.extension.ExtensionLoader;
import com.siman.qrpc.remoting.constans.RpcConstants;
import com.siman.qrpc.remoting.model.RpcMessage;
import com.siman.qrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义协议编码器
 *
 *   <pre>
 *   0     1     2     3     4        5     6     7     8     9          10       11     12    13    14   15
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+-----------+-----+-----+-----+
 *   |   magic   code        |version | Full length         | messageType| codec| RequestId                   |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 *  4B  magic code（魔法数）  1B version（版本）  4B full length（消息长度）  1B messageType（消息类型）
 *  1B codec（序列化类型）    4B  requestId（请求的Id）
 *  body（object类型数据）
 *  </pre>
 *
 * @author SiMan
 * @date 2021/1/21 0:32
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
        try {
            // 消息头长度
            int fullLength = RpcConstants.HEAD_LENGTH;
            // 获取消息类型
            byte messageType = rpcMessage.getMessageType();
            // 写入魔数
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            out.writeByte(RpcConstants.VERSION);
            // 留出位置写入数据包的长度
            out.writerIndex(out.writerIndex() + 4);
            // 设置消息类型
            out.writeByte(rpcMessage.getMessageType());
            // 设置序列化
            out.writeByte(rpcMessage.getCodec());
            out.writeInt(ATOMIC_INTEGER.getAndDecrement());
            byte[] bodyBytes = null;

            // 不是心跳
            if (messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE
                    && messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                // 对象序列化
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                        .getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }

            // 获取当前读指针
            int writerIndex = out.writerIndex();
            out.writerIndex(writerIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            // 写入长度
            out.writeInt(fullLength);
            // 重置
            out.writerIndex(writerIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }
    }
}
