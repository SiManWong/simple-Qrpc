package com.siman.qrpc.extension;

/**
 * @author SiMan
 * @date 2021/2/5 23:06
 */

public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
