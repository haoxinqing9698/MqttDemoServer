package net.itfeng.mqttdemoserver.util;

import net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TODO 描述类的用途，不修改请删除此行
 *
 * @author fengxubo
 * @since 2024/1/28 16:54
 * 
 */
public class MockTestDataUtil {


    public static net.itfeng.mqttdemoserver.protocol.TestDataTransOuterClass.TestDataTrans buildTestDataTransObject(int size,String clientId) {
        List<TestDataTransOuterClass.ADASRecognizedReq> list = buildADASRecognizedReqList(size);
        return TestDataTransOuterClass.TestDataTrans.newBuilder()
                .setClientId(clientId)
                .setStartTimeMillis(System.currentTimeMillis())
                .addAllAdas(list)
                .build();
    }

    private static List<TestDataTransOuterClass.ADASRecognizedReq> buildADASRecognizedReqList(int uploadMsgSize) {
        // 单条数据大小220B，外层数据大小126B基于此计算需要多少个数据
        int size = (uploadMsgSize - 30) / 220;
        List<TestDataTransOuterClass.ADASRecognizedReq> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TestDataTransOuterClass.ADASRecognizedReq adasRecognizedReq = TestDataTransOuterClass.ADASRecognizedReq.newBuilder()
                    .setAlt(123.12)
                    .setCardId("9925")
                    .setCityCode("100010")
                    .setDataAccuracy(1)
                    .setCollectIPs("127.0.0.1")
                    .setColor("black")
                    .setDistance(50)
                    .setDistanceX(30)
                    .setDistanceY(40)
                    .setDrawlevel(1)
                    .setHeading(90.001d)
                    .setLat(39.049201954348256)
                    .setLon(117.05879654989144)
                    .setMortonCode(System.currentTimeMillis())
                    .setLaneNum(0)
                    .setVehicleType(9)
                    .setSatelliteTime(System.currentTimeMillis())
                    .setTileId("9925")
                    .setUuid(UUID.randomUUID().toString())
                    .setSpeed(10)
                    .setCollectIPs("192.168.25.13,192.168.25.14,192.168.25.15,192.168.25.16")
                    .build();
            list.add(adasRecognizedReq);
        }
        return list;
    }
}
