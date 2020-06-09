package net.shopin.utils.history;

import net.shopin.utils.history.interceptors.HistoryTableInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @title: net.shopin.utils.history.autoconfiguration.HistoryStarterAutoConfiguration
 * @description: 历史表拦截器 自动配置
 * @author: qiaomg
 * @date: 2020/6/8 10:16
 * @version: V1.0
 */
@Configuration
public class HistoryTableAutoConfiguration {
    @Bean
    public HistoryTableInterceptor doHistroyInterceptor() {
        return HistoryTableInterceptor.getInstance();
    }
}
