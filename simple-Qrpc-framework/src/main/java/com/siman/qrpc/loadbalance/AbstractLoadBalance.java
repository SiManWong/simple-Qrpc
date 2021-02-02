package com.siman.qrpc.loadbalance;

import java.util.List;

/**
 * @author SiMan
 * @date 2021/1/31 17:16
 */

public abstract class AbstractLoadBalance implements LoadBalance{

    @Override
    public String selectServiceAddress(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    /**
     * 负载均衡选择
     * @param serviceAddresses
     * @return
     */
    protected abstract String doSelect(List<String> serviceAddresses);
}
