package net.itfeng.mqttdemoserver.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.mqttdemoserver.context.TimeDiffContext;
import net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass;
import net.itfeng.mqttdemoserver.util.MessageIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * 计算服务端与客户端时间差
 *
 * @author fengxubo
 * @since 2024/1/8 19:11
 * 
 */
@Slf4j
@Service
public class ClientTimeDiffCalculator {
    private static final Cache<String, Queue<net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.ClientTimeDiff>> TIME_DIFF_DATA_CACHE = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(5000).build();

    @Autowired
    private MessageAsyncPublishService messageAsyncPublishService;

    // 数据上传时间差主题
    @Value("${mqtt.push_time_diff_topic}")
    private String pushTimeDiffTopic;

    public void addTimeDiff(String clientId, net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.ClientTimeDiff timeDiff) {
        Queue<net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.ClientTimeDiff> queue = TIME_DIFF_DATA_CACHE.getIfPresent(clientId);
        if (queue == null) {
            queue = new java.util.concurrent.ConcurrentLinkedDeque<>();
            TIME_DIFF_DATA_CACHE.put(clientId, queue);
        }
        queue.offer(timeDiff);
        if(queue.size()>5){
            queue.poll();
        }
        // 这里不能删除，用于续期
        TIME_DIFF_DATA_CACHE.put(clientId, queue);
    }

    /**
     * 定时计算时间差
     * 每秒运行
     */
    @Scheduled(cron = "* * * * * ?")
    public void calculate(){
        Map<String, Queue<TestDataTransOuterClass.ClientTimeDiff>> temp = new HashMap<>(TIME_DIFF_DATA_CACHE.asMap());

//        log.info("开始计算时间差，当前缓存数量：{}", temp.size());
        temp.forEach( (k,v)->{
            TestDataTransOuterClass.ClientTimeDiff minIntervalObject = findMinInterval(v);
            // 计算时间差 = 发出时间 + 数据传输时间 - 客户端收到时间
            long timeDiff = minIntervalObject.getStartTimeMillis() + (minIntervalObject.getEndTimeMillis() - minIntervalObject.getStartTimeMillis()) / 2 - minIntervalObject.getTimeMillis();
//            log.info("计算时间差，clientId:{}, 最小间隔：{}ms", entry.getKey(), timeDiff);
            TimeDiffContext.putTimeDiff(minIntervalObject.getClientId(), timeDiff);
        });
    }

    /**
     * 每秒发送时间校准数据到所有客户端
     */
    @Scheduled(cron = "* * * * * ?")
    public void sendTimeDiff(){
        long now = System.currentTimeMillis();
        TestDataTransOuterClass.ClientTimeDiff clientTimeDiff = TestDataTransOuterClass.ClientTimeDiff.newBuilder()
                .setMsgId(MessageIdUtil.getMessageId())
                .setStartTimeMillis(now)
                .build();
        try{
            messageAsyncPublishService.publish(pushTimeDiffTopic,clientTimeDiff.toByteArray());
        } catch (Exception e) {
            log.error("发送时间差失败 {}", pushTimeDiffTopic, e);
        }
    }

    /**
     * 根据 queue 找到最小时间间隔
     * @param queue 传入的队列
     * @return 最小时间间隔
     */
    private TestDataTransOuterClass.ClientTimeDiff findMinInterval(Queue<TestDataTransOuterClass.ClientTimeDiff> queue) {
        if (queue == null || queue.isEmpty()) {
            return null;
        }
        long min = Long.MAX_VALUE;
        TestDataTransOuterClass.ClientTimeDiff result = null;
        for (TestDataTransOuterClass.ClientTimeDiff object : queue) {
            long diff = object.getEndTimeMillis() - object.getStartTimeMillis();
            if (diff < min) {
                min = diff;
                result = object;
            }
        }
        return result;
    }


    /**
     * 不能删除，用于单元测试
     * @param clientId 客户端id
     * @return 当前队列的大小
     */
    public int getSize(String clientId){
        Queue<net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.ClientTimeDiff> queue = TIME_DIFF_DATA_CACHE.getIfPresent(clientId);
        if (queue == null) {
            return 0;
        }
        return queue.size();
    }

}
