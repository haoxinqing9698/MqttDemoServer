package net.itfeng.mqttdemoserver.client;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import net.itfeng.mqttdemoserver.handler.MyMessageHandler;
import net.itfeng.mqttdemoserver.util.ClientIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * mqtt订阅者
 */
@Service
@Slf4j
public class MqttSubscriber{
    private static final AtomicBoolean connected = new AtomicBoolean(false);
    private MqttClient mqttClient;

    @Value("${mqtt.subscriber.brokers}")
    private String broker;
    @Value("${mqtt.subscriber.username: userNull}")
    private String userName;

    @Value("${mqtt.subscriber.password: passwordNull}")
    private String password;

    @Autowired
    private List<MyMessageHandler> myMessageHandlers;


    @PostConstruct
    public void start() throws MqttException {
        String clientId = "server_subscriber_"+ClientIdUtil.getClientId(); // 你的客户端ID。
        // 创建一个新的MqttClient实例，使用MemoryPersistence作为存储引擎。
        mqttClient = new MqttClient(broker, clientId, new MemoryPersistence());
        // 创建连接选项并设置自动重新连接为true。
        MqttConnectOptions connOpts = new MqttConnectOptions();
        if(!"userNull".equals(userName)){
            connOpts.setUserName(userName);
            connOpts.setPassword(password.toCharArray());
        }
        connOpts.setAutomaticReconnect(true);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connected.set(false);
                System.out.println("Connection lost. Reconnecting...");
                reconnect(connOpts);
            }
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                myMessageHandlers.forEach(handler -> {
                    if(handler.isSupport(topic)){
                        handler.handle(message.getPayload());
                    }
                });
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("{} delivered.",token.getMessageId());
            }
        });
        reconnect(connOpts);
    }

    @PreDestroy
    public void stop() throws MqttException {
        if (mqttClient != null) {
            mqttClient.disconnect();
            mqttClient.close();
        }
    }

    private void reconnect(MqttConnectOptions connOpts) {
        while (!connected.get()) {
            try {
                mqttClient.connect(connOpts);
                connected.set(true);
                //  订阅主题
                myMessageHandlers.forEach(myMessageHandler -> {
                    try {
                        mqttClient.subscribe(myMessageHandler.getMessageType());
                        log.info("订阅topic:{}", myMessageHandler.getMessageType());
                    } catch (MqttException e) {
                        log.error("订阅topic异常"+ myMessageHandler.getMessageType());
                        throw new RuntimeException(e);
                    }
                });
            } catch (MqttException e) {
                log.error("Failed to reconnect. Trying again in 5 seconds.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    log.error(interruptedException.getMessage(), interruptedException);
                }
            }
        }
    }


}

