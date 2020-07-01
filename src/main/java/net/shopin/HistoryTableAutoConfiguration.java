package net.shopin;

import net.shopin.history.interceptors.HistoryTableInterceptor;
import net.shopin.history.properties.HistoryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @title: HistoryStarterAutoConfiguration
 * @description: 历史表拦截器 自动配置
 * @author: qiaomg
 * @date: 2020/6/8 10:16
 * @version: V1.0
 */
@Configuration
@EnableConfigurationProperties(HistoryProperties.class)
public class HistoryTableAutoConfiguration {
    @Bean
    public HistoryTableInterceptor doHistroyInterceptor() {
        return HistoryTableInterceptor.getInstance();
    }
}
