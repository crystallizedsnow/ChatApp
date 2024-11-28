package com.chatapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Bean
    public TaskScheduler taskScheduler() {
        return new ThreadPoolTaskScheduler(); // 创建线程池任务调度器
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 设置客户端订阅路径前缀，
        // 订阅Broker名称:topic 代表发布广播，即群发
        // queue 代表点对点，即发指定用户
        config.enableSimpleBroker("/topic", "/queue")
                // 设置后端心跳配置，提供 TaskScheduler
                .setHeartbeatValue(new long[]{20000, 20000})
                .setTaskScheduler(taskScheduler());

        // 设置应用程序消息路径前缀，用于处理前端发来的消息。send命令时需要带上/app前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀，用于点对点消息发送
        // 修改convertAndSendToUser方法前缀
        // 点对点使用的订阅前缀（客户端订阅路径上会体现出来），
        config.setUserDestinationPrefix("/user/");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 设置 WebSocket 端点，允许跨域
        // 注册一个名字为"/ws" 的endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }
}

