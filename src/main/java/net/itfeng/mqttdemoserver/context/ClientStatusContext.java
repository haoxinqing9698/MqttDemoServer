package net.itfeng.mqttdemoserver.context;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * 客户端状态上下文, 用于存储在线客户端信息
 *
 * @author fengxubo
 * @since 2024/1/9 11:08
 * 
 */
public class ClientStatusContext {
    public static Cache<String, Long> CLIENT_CACHE = CacheBuilder
            .newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(5000).build();

    public static void putClient(String clientId) {
        CLIENT_CACHE.put(clientId, System.currentTimeMillis());
    }

    public static Collection<String> getAllOnlineClient() {
        return new HashSet<>(CLIENT_CACHE.asMap().keySet());
    }
}
