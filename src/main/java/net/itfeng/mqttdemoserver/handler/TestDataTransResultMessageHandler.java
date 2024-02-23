package net.itfeng.mqttdemoserver.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import net.itfeng.mqttdemoserver.context.ClientStatusContext;
import net.itfeng.mqttdemoserver.context.MessageContext;
import net.itfeng.mqttdemoserver.context.TimeDiffContext;
import net.itfeng.mqttdemoserver.util.TestDataTransTextLogUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
@Service
public class TestDataTransResultMessageHandler implements MyMessageHandler{

    @Value("${mqtt.upload_msg_result_topic}")
    private String uploadMsgResultTopic;

    public void handle(byte[] messageByte)  {
        net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.TestDataTransResult testDataTransResult;
        try {
            testDataTransResult = net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.TestDataTransResult.parseFrom(messageByte);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
        String sn = testDataTransResult.getClientId();
        // 记录客户端状态
        ClientStatusContext.putClient(sn);
        // 记录开始时间
        long startTimeMillis = testDataTransResult.getStartTimeMillis();
        long endTimeMillis = testDataTransResult.getReceivedTimeMillis();
        // 获取当前服务与客户端的时间差
        long diff = TimeDiffContext.getTimeDiff(sn);
        // 计算时延
        long latency = endTimeMillis - startTimeMillis + diff;
        Integer size = MessageContext.getLength(testDataTransResult.getClientId(),testDataTransResult.getMsgId());
        if(size==null){
            size =0;
        }
        TestDataTransTextLogUtil.log(sn+ " "+ size + " " + latency +" "+ diff);

    }

    @Override
    public String getMessageType() {
        return uploadMsgResultTopic;
    }

    /**
     * 服务端监听的是所有客户端的mqtt topic,而本消息处理器只处理与当前消息类型匹配的topic数据
     * 由于每次使用正则校验消耗性能，因此通过缓存将复杂度将到 O(1)
     */
    private static final Set<String> mySupportTopic = Collections.synchronizedSet(new HashSet<>());
    private static final Set<String> myUnSupportTopic = Collections.synchronizedSet(new HashSet<>());
    @Override
    public boolean isSupport(String topic) {
        if (mySupportTopic.contains(topic)) {
            return true;
        }
        if (myUnSupportTopic.contains(topic)) {
            return false;
        }
        String regex = "^"+uploadMsgResultTopic.replace("+","(.+)")+"$";
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
