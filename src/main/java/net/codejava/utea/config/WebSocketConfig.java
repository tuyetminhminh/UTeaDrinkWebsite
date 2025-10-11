package net.codejava.utea.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint client sẽ connect: ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // cho phép fallback SockJS
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // prefix cho các message từ server gửi ra
        config.enableSimpleBroker("/topic");
        // prefix cho client gửi lên server
        config.setApplicationDestinationPrefixes("/app");
    }
}
