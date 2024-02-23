package net.itfeng.mqttdemoserver.client;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import net.itfeng.mqttdemoserver.util.ClientIdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@Slf4j
@Component
public class MqttPublisher {
    private MqttClient mqttClient;
    @Value("${mqtt.publisher.brokers}")
    private String broker;
    @Value("${mqtt.publisher.username: userNull}")
    private String userName;
    @Value("${mqtt.publisher.password: passwordNull}")
    private String password;
    @PostConstruct
    public void init() throws MqttException {
        String clientId = "server_publisher_"+ ClientIdUtil.getClientId();
        // 创建 MQTT 客户端
        mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());

        // 创建发布者并设置连接参数
        MqttConnectOptions connOpts = new MqttConnectOptions();
        if(!"userNull".equals(userName)){
            connOpts.setUserName(userName);
            connOpts.setPassword(password.toCharArray());
        }
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);

        // 连接 MQTT 代理
        mqttClient.connect(connOpts);
        System.out.println("Connected to broker: " + broker);
    }

    /**
     * 发布消息
     * @param topic 主题
     * @param message pb格式的消息内容
     */
    public void publish(String topic,byte[] message) {
        MqttMessage mqttMessage = new MqttMessage((message));
            mqttMessage.setQos(0);
        try {
            mqttClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() throws MqttException {
        if (mqttClient != null) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

}

