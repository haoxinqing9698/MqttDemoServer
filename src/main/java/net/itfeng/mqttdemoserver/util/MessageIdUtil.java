package net.itfeng.mqttdemoserver.util;

import java.util.UUID;

/**
 * 生成唯一ID
 *
 * @author fengxubo
 * @since 2024/1/27 11:56
 * 
 */
public class MessageIdUtil {
    public static String getMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
