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
    public static String entityPath;
    public static String serverName;

    public void setEntityPath(String path) {
        entityPath = path;
    }

    public void setServerName(String name) {
        serverName = name;
    }
}
