package net.itfeng.mqttdemoserver.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.mqttdemoserver.service.ClientTimeDiffCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试数据传输消息处理
 *
 * @author fengxubo
 * @since 2024/1/8 15:46
 * 
 */
@Slf4j
@Service
public class TimeDiffMessageHandler implements MyMessageHandler {
    @Value(value = "${mqtt.upload_time_diff_topic}")
    private String uploadTimeDiffTopic;

    @Autowired
    private ClientTimeDiffCalculator clientTimeDiffCalculator;


    public void handle(byte[] messageByte) {
        long now = System.currentTimeMillis();
        TestDataTransOuterClass.ClientTimeDiff clientTimeDiff = null;
        try {
            clientTimeDiff = TestDataTransOuterClass.ClientTimeDiff.parseFrom(messageByte);
        } catch (InvalidProtocolBufferException e) {
            log.error("解析数据传输消息失败", e);
            throw new RuntimeException(e);
        }
        TestDataTransOuterClass.ClientTimeDiff clientTimeDiffNew = TestDataTransOuterClass.ClientTimeDiff.newBuilder()
                .setClientId(clientTimeDiff.getClientId())
                .setMsgId(clientTimeDiff.getMsgId())
                .setStartTimeMillis(clientTimeDiff.getStartTimeMillis())
                .setTimeMillis(clientTimeDiff.getTimeMillis())
                .setEndTimeMillis(now)
                .build();
        // 交给下一个流程执行时间比较，计算时差
        clientTimeDiffCalculator.addTimeDiff(clientTimeDiffNew.getClientId(),clientTimeDiffNew);

    }

    @Override
    public String getMessageType() {
        return uploadTimeDiffTopic;
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
        String regex = "^"+uploadTimeDiffTopic.replace("+","(.+)")+"$";
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
