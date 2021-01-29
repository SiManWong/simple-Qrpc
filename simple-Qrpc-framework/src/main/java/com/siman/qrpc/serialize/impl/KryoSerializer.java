package com.siman.qrpc.serialize.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.siman.qrpc.exception.SerializeException;
import com.siman.qrpc.remoting.model.RpcRequest;
import com.siman.qrpc.remoting.model.RpcResponse;
import com.siman.qrpc.serialize.Serializer;
import com.siman.qrpc.serialize.SerializerAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author SiMan
 * @date 2021/1/21 1:15
 */

public class KryoSerializer implements Serializer {

    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
       Kryo kryo = new Kryo();
       kryo.register(RpcResponse.class);
       kryo.register(RpcRequest.class);
       //默认值为true,是否关闭注册行为,关闭之后可能存在序列化问题，一般推荐设置为 true
       kryo.setReferences(true);
        //默认值为false,是否关闭循环引用，可以提高性能，但是一般不推荐设置为 true
       kryo.setRegistrationRequired(false);

       return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            // 将对象序列化为 byte 数组
            kryo.writeObject(output, obj);
            KRYO_THREAD_LOCAL.remove();
            return output.toBytes();
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)){
            Kryo kryo = KRYO_THREAD_LOCAL.get();
            Object o = kryo.readObject(input, clazz);
            KRYO_THREAD_LOCAL.remove();

            return clazz.cast(o);
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
    }

    @Override
    public byte getSerializerAlgorithm() {
        return SerializerAlgorithm.KRYO;
    }
}
