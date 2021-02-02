package com.siman.qrpc.loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @author SiMan
 * @date 2021/1/31 17:20
 */
public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    protected String doSelect(List<String> serviceAddresses) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
