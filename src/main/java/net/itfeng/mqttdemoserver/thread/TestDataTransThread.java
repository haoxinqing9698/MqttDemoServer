package net.itfeng.mqttdemoserver.thread;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.mqttdemoserver.service.MessageAsyncPublishService;
import net.itfeng.mqttdemoserver.util.MockTestDataUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 测试数据传输线程类
 *
 * @author fengxubo
 * @since 2024/1/28 14:30
 * 
 */
@Slf4j
public class TestDataTransThread implements Runnable {
    private static final long MAX_EXPIRE_TIME = 10  * 1000;
    @Getter
    private boolean stopped = false;

    private final AtomicInteger i = new AtomicInteger(1);
    /**
     * 最后更新时间，用于判断是否需要继续下发数据
     * 1. 客户端状态为在线状态，则每3秒更新一次该时间
     * 2. 客户端状态为离线状态，则停止更新该时间
     * 3. 停止更新超过 MAX_EXPIRE_TIME 秒后，停止下发数据
     */
    @Setter
    private long lastUpdateTime;

    private final String clientId;
    private final String pushMsgTopic;

    private final int[] pushMsgSize;

    private final int pushMsgCount;

    private final int pushMsgInterval;

    private final MessageAsyncPublishService messageAsyncPublishService;

    public TestDataTransThread(String clientId, String topic, int[] pushMsgSize, int pushMsgCount, int pushMsgInterval, MessageAsyncPublishService messageAsyncPublishService) {
        this.clientId = clientId;
        this.pushMsgTopic = topic;
        this.pushMsgSize = pushMsgSize;
        this.pushMsgCount = pushMsgCount;
        this.pushMsgInterval = pushMsgInterval;
        this.lastUpdateTime = System.currentTimeMillis();
        this.messageAsyncPublishService = messageAsyncPublishService;
    }

    @Override
    public void run() {
        while(System.currentTimeMillis() - lastUpdateTime < MAX_EXPIRE_TIME) {
            int ivalue = i.incrementAndGet();
            int index = ivalue / pushMsgCount;
            // 如果计划数据还没发送完毕
            if (index < pushMsgSize.length) {
                int size = pushMsgSize[index];
                // 构造数据
                net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.TestDataTrans testDataTrans = MockTestDataUtil.buildTestDataTransObject(size,clientId);
                // 发送数据
                messageAsyncPublishService.publish(pushMsgTopic, testDataTrans.toByteArray());
            }
            try {
                TimeUnit.MILLISECONDS.sleep(pushMsgInterval);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.info("{} 发送数据线程终止  *************************", clientId);
        stopped = true;
    }
}
