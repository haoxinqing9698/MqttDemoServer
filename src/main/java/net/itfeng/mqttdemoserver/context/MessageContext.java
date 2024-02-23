package net.itfeng.mqttdemoserver.context;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 用于保存发送消息的信息-消息的长度
 *
 * @author fengxubo
 * @since 2024/2/23 13:21
 */
public class MessageContext {
    /**
     * 每条消息保留3秒，根据此估算，如果发送频率是50帧，则一个客户端的数据最多为150条
     */
    public static Cache<String, Integer> CLIENT_MSG_CACHE = CacheBuilder
            .newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(5000).build();

    public static void putClient(String clientId,String msgId,int length) {
        CLIENT_MSG_CACHE.put(clientId+msgId, length);
    }

    public static Integer getLength(String clientId,String msgId) {
        return CLIENT_MSG_CACHE.getIfPresent(clientId+msgId);
    }
}
