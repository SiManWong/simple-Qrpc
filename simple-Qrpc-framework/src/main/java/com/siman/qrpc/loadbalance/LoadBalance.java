package com.siman.qrpc.loadbalance;

import java.util.List;

/**
 * @author SiMan
 * @date 2021/1/31 17:15
 */

public interface LoadBalance {
    /**
     * 在已有服务提供地址列表中选择一个
     * @param serviceAddresses
     * @return
     */
    String selectServiceAddress(List<String> serviceAddresses);
}
