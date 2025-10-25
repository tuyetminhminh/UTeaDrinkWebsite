package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")             // endpoint client connect
                .setAllowedOriginPatterns("*")
                .withSockJS();                  // bật SockJS fallback
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // nơi client subscribe để NHẬN tin
        registry.enableSimpleBroker("/topic", "/queue");
        // nơi client gửi tin lên server
        registry.setApplicationDestinationPrefixes("/app");
        // cho phép gửi trực tiếp theo user nếu cần
        registry.setUserDestinationPrefix("/user");
    }
    // thêm vào WebSocketConfig:

}
