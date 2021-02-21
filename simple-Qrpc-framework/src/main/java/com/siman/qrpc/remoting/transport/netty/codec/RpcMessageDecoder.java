package com.siman.qrpc.remoting.transport.netty.codec;

import com.siman.qrpc.enums.SerializationTypeEnum;
import com.siman.qrpc.extension.ExtensionLoader;
import com.siman.qrpc.remoting.constans.RpcConstants;
import com.siman.qrpc.remoting.model.RpcMessage;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import com.siman.qrpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 自定义协议解码器
 * <pre>
 *   0     1     2     3     4        5     6     7     8     9          10       11     12    13    14   15
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+-----------+-----+-----+-----+
 *   |   magic   code        |version | Full length         | messageType| codec| RequestId                   |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）  1B version（版本）  4B full length（消息长度）  1B messageType（消息类型）
 * 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} 基于长度域拆包器，由Netty提供，用于解决TCP拆包粘包问题
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 * </p>
 * @author SiMan
 * @date 2021/1/21 13:54
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // default is 8M
        this(RpcConstants.MAX_FRAME_LENGTH);
    }

    public RpcMessageDecoder(int maxFrameLength) {
        /*
        maxFrameLength, 包的最大长度
        lengthFieldOffset, 长度域的偏移量（4B的魔数 + 1B的版本号）
        lengthFieldLength, 长度域占用字节
        lengthAdjustment, 数据包调整长度
        initialBytesToStrip 剥离字节数
        */
        super(maxFrameLength, 5, 4, -9, 0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decode;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf in) {
        // 读取前4个magic对比一下
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }

        // 判断版本号
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }

        // 消息长度
        int fullLength = in.readInt();
        // 消息类型
        byte messageType = in.readByte();
        // 序列化类型
        byte codecType = in.readByte();
        // 请求id
        int requestId = in.readInt();
        // 构建 RpcMessage
        RpcMessage rpcMessage = new RpcMessage();
        rpcMessage.setMessageType(messageType);
        rpcMessage.setCodec(codecType);
        rpcMessage.setRequestId(requestId);

        // 心跳请求
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
        } else if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            // 心跳响应
            rpcMessage.setData(RpcConstants.PONG);
        } else {
            int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
            if (bodyLength > 0) {
                byte[] bs = new byte[bodyLength];
                in.readBytes(bs);
                // 序列化方式
                String codecName = SerializationTypeEnum.getName(codecType);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                // 反序列化得到 RpcRequest 或 RpcResponse
                if (messageType == RpcConstants.REQUEST_TYPE) {
                    RpcRequest rpcRequest = serializer.deserialize(bs, RpcRequest.class);
                    rpcMessage.setData(rpcRequest);
                } else {
                    RpcResponse rpcResponse = serializer.deserialize(bs, RpcResponse.class);
                    rpcMessage.setData(rpcResponse);
                }
            }
        }
        return rpcMessage;
    }
}
