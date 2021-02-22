package com.siman.qrpc.compress;

import com.siman.qrpc.extension.SPI;

/**
 * @author SiMan
 * @date 2021/2/22 17:16
 */
@SPI
public interface Compress {
    /**
     * 压缩
     * @param bytes
     * @return
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压
     * @param bytes
     * @return
     */
    byte[] decompress(byte[] bytes);
}
