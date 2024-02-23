package net.itfeng.mqttdemoserver.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.mqttdemoserver.context.ClientStatusContext;
import net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass;
import net.itfeng.mqttdemoserver.service.MessageAsyncPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间校准消息处理器
 *
 * @author fengxubo
 * @since 2024/1/8 15:46
 * 
 */
@Slf4j
@Service
public class HeartbeatPingMessageHandler implements MyMessageHandler{

    /**
     * 数据上传ping主题, 客户端发送给服务端，topic中包含占位符，需要使用client_id替换
     */
    @Value("${mqtt.upload_heartbeat_topic}")
    private String heartbeatPingTopic;

    @Value("${mqtt.push_heartbeat_pong_topic}")
    private String heartbeatPongTopic;

    @Autowired
    private MessageAsyncPublishService messageAsyncPublishService;



    @Async("mqttMessageHandlerPool")
    public void handle(byte[] messageBytes)  {
        TestDataTransOuterClass.HeartBeatPing heartBeatPing = null;
        try {
            heartBeatPing = TestDataTransOuterClass.HeartBeatPing.parseFrom(messageBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("解析心跳响应消息失败", e);
        }
        if(heartBeatPing == null){
            return;
        }
        // 收到心跳记录状态
        ClientStatusContext.putClient(heartBeatPing.getClientId());
        log.info("收到心跳ping消息, msg_id:{}, client_id:{},sendTime:{}, timestamp:{}", heartBeatPing.getMsgId(), heartBeatPing.getClientId(),heartBeatPing.getStartTimeMillis(),System.currentTimeMillis());
        // 回复pong
        TestDataTransOuterClass.HeartBeatPong heartBeatPong = TestDataTransOuterClass.HeartBeatPong.newBuilder()
                .setMsgId(heartBeatPing.getMsgId())
                .setReceivedTimeMillis(System.currentTimeMillis())
                .build();
        messageAsyncPublishService.publish(String.format(heartbeatPongTopic,heartBeatPing.getClientId()), heartBeatPong.toByteArray());

    }

    @Override
    public String getMessageType() {
        return this.heartbeatPingTopic;
    }

    private static Set<String> mySupportTopic = Collections.synchronizedSet(new HashSet<>());
    private static Set<String> myUnSupportTopic = Collections.synchronizedSet(new HashSet<>());
    @Override
    public boolean isSupport(String topic) {
        if (mySupportTopic.contains(topic)) {
            return true;
        }
        if (myUnSupportTopic.contains(topic)) {
            return false;
        }
        String regex = "^"+heartbeatPingTopic.replace("+","(.+)")+"$";
        // 编译正则表达式为Pattern对象
        Pattern pattern = Pattern.compile(regex);
        // 创建Matcher对象来寻找匹配
        Matcher matcher = pattern.matcher(topic);
        // 使用matches方法检查整个字符串是否与正则表达式匹配
        if(matcher.matches()){
            mySupportTopic.add(topic);
            return true;
        }else{
            myUnSupportTopic.add(topic);
            return false;
        }
    }
}
