package net.itfeng.mqttdemoserver.util;

import lombok.extern.slf4j.Slf4j;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

/**
 * 基于MAC地址生成唯一client_id
 *
 * @author fengxubo
 * @since 2024/1/9 11:36
 * 
 */
@Slf4j
public class ClientIdUtil {
    /**
     * 服务端生成client_id，格式：mac地址_UUID
     * @return 唯一client_id
     */
    public static String getClientId() {
        String mac = getMacAddress(); // 你的MAC地址。
        return mac +"_" + UUID.randomUUID();
    }

    private static String getMacAddress() {
        String mac = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (networkInterface.isUp() && !networkInterface.isVirtual() && !networkInterface.isLoopback() &&
                        (networkInterface.getName().equals("eth0") || networkInterface.getName().equals("en0") || networkInterface.getName().equals("ens192"))) {
                    byte[] macBytes = networkInterface.getHardwareAddress();

                    if (macBytes != null) {
                        StringBuilder macAddress = new StringBuilder();
                        for (byte b : macBytes) {
                            macAddress.append(String.format("%02X:", b));
                        }

                        if (macAddress.length() > 0) {
                            macAddress.deleteCharAt(macAddress.length() - 1); // 移除末尾的冒号
                        }

                        mac = macAddress.toString();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("获取本机mac地址出错", e);
        }
        return mac;
    }
}
