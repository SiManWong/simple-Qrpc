package com.siman.qrpc.remoting.transport.netty.client;

import com.siman.qrpc.factory.SingletonFactory;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储并获取 channel 对象
 *
 * @author SiMan
 * @date 2021/1/20 16:07
 */
@Slf4j
public class ChannelProvider {

    private static final Map<String, Channel> channelMap;
    private static final NettyRpcClient nettyRpcClient;

    static {
        channelMap = new ConcurrentHashMap<>();
        nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    public static Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 判断是否有对应地址的连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // 判断连接是否可用
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        // 否则，重新连接获取 Channel
        Channel channel = nettyRpcClient.doConnect(inetSocketAddress);
        channelMap.put(key, channel);

        return channel;
    }



}
