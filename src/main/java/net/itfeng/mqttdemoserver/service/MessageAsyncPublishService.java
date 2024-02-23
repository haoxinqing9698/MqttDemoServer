package net.itfeng.mqttdemoserver.service;

import net.itfeng.mqttdemoserver.client.MqttPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步代理处理
 *
 * @author fengxubo
 * @since 2024/1/9 20:16
 * 
 */
@Service
public class MessageAsyncPublishService {
    @Autowired
    private MqttPublisher mqttPublisher;

    @Async("messagePublisherPool")
    public void publish(String topic, byte[] message) {
        mqttPublisher.publish(topic, message);
    }
}
