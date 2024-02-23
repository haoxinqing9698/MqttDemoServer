package net.itfeng.mqttdemoserver.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 测试数据转换文本日志，并输出到文件
 *
 * @author fengxubo
 * @since 2024/1/8 16:15
 * 
 */
@Slf4j
public class TestDataTransTextLogUtil {
    /**
     * 在logback里面配置打印格式
     * @param msg 打印内容到指定日志文件
     */
    public static void log(String msg) {
        log.debug(msg);
    }
}
