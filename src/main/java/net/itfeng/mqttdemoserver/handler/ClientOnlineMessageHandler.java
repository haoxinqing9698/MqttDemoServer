package net.itfeng.mqttdemoserver.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.mqttdemoserver.context.ClientStatusContext;
import net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass;
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
public class ClientOnlineMessageHandler implements MyMessageHandler{

    /**
     * 数据上传时间差主题, 客户端发送给服务端，topic中包含占位符，需要使用client_id替换
     */
    @Value("${mqtt.upload_online_msg_topic}")
    private String uploadOnlineMsgTopic;

    @Async("mqttMessageHandlerPool")
    public void handle(byte[] messageBytes)  {
        TestDataTransOuterClass.OnlineEvent onlineEvent = null;
        try {
            onlineEvent = TestDataTransOuterClass.OnlineEvent.parseFrom(messageBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("解析心跳响应消息失败", e);
        }
        if (onlineEvent == null) {
            return;
        }
        ClientStatusContext.putClient(onlineEvent.getClientId());
        log.info("收到上线 OnlineEvent 消息, msg_id:{}, clinet_id:{}", onlineEvent!=null?onlineEvent.getMsgId():"null", onlineEvent!=null?onlineEvent.getClientId():null);
    }

    @Override
    public String getMessageType() {
        return uploadOnlineMsgTopic;
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
        String regex = "^"+uploadOnlineMsgTopic.replace("+","(.+)")+"$";
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
