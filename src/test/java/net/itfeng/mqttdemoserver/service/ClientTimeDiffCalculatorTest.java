package net.itfeng.mqttdemoserver.service;

import static org.junit.jupiter.api.Assertions.*;

import lombok.extern.slf4j.Slf4j;
import net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.ClientTimeDiff;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;
@Slf4j
public class ClientTimeDiffCalculatorTest {

    @Test
    void addTimeDiff() {
        ClientTimeDiffCalculator clientTimeDiffCalculator = new ClientTimeDiffCalculator();
        String sn = "test";
        for (int i = 0; i < 20; i++) {
            ClientTimeDiff mockClientTimeDiff = Mockito.mock(ClientTimeDiff.class);
            clientTimeDiffCalculator.addTimeDiff(sn, mockClientTimeDiff);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(i<5){
                assertEquals(i+1, clientTimeDiffCalculator.getSize(sn));
            }else{
                // 当数据量大于5时，只保留最新的5个数据
                assertEquals(5, clientTimeDiffCalculator.getSize(sn));
            }
        }
    }
}
