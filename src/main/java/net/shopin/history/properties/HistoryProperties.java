package net.shopin.history.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @title: HistoryProperties
 * @description: TODO(用一句话描述该文件做什么)
 * @author: qmg
 * @date: 2020/6/8 11:08
 * @version: V1.0
 */
@Configuration
@ConfigurationProperties(prefix = "history.config")
public class HistoryProperties {
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String INSERT = "INSERT";

    public static String entityPath;
    public static String serverName;


    public static String getServerName() {
        return serverName;
    }

    public static void setServerName(String serverName) {
        HistoryProperties.serverName = serverName;
    }

    public static String getEntityPath() {
        return entityPath;
    }

    public static void setEntityPath(String entityPath) {
        HistoryProperties.entityPath = entityPath;
    }
}
