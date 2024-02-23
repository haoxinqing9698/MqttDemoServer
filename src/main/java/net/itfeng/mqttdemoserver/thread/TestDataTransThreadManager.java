package net.itfeng.mqttdemoserver.thread;

import net.itfeng.mqttdemoserver.service.MessageAsyncPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试数据传输线程管理类,用于线程对象的开启与关闭控制
 *
 * @author fengxubo
 * @since 2024/1/28 14:23
 * 
 */
@Service
public class TestDataTransThreadManager {
    /**
     * 线程对象集合
     * key: clientId
     * value: TestDataTransThread,下发测试数据的线程对象
     */
    private static final Map<String, TestDataTransThread> THREAD_MAP = new ConcurrentHashMap<>();

    @Autowired
    private MessageAsyncPublishService messageAsyncPublishService;

    public boolean checkRunning(String clientId) {
        TestDataTransThread t = THREAD_MAP.get(clientId);
        if(t!=null){
            if(t.isStopped()){
                THREAD_MAP.remove(clientId);
                return false;
            }
            return true;
        }else{
            return false;
        }

    }

    public boolean updateLastTime(String clientId) {
        if (checkRunning(clientId)) {
            THREAD_MAP.get(clientId).setLastUpdateTime(System.currentTimeMillis());
            return true;
        } else {
            return false;
        }
    }

    public boolean startThread(String clientId, String myTopic,int[] pushMsgSize, int pushMsgCount, int pushMsgInterval) {
        if (THREAD_MAP.containsKey(clientId)) {
            return false;
        } else {
            TestDataTransThread testDataTransThread = new TestDataTransThread(clientId, myTopic, pushMsgSize,pushMsgCount,pushMsgInterval,messageAsyncPublishService);
            new Thread(testDataTransThread, "TestDataTransThread-"+clientId.replace(':','-')).start();
            THREAD_MAP.put(clientId, testDataTransThread);
            return true;
        }
    }


}
