// net/codejava/utea/config/WebSocketConfig.java
package net.codejava.utea.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

// net/codejava/utea/config/WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WsJwtHandshakeInterceptor wsJwtHandshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry reg) {
        reg.addEndpoint("/ws")
                .addInterceptors(wsJwtHandshakeInterceptor)  // << thêm dòng này
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry cfg) {
        cfg.setApplicationDestinationPrefixes("/app");
        cfg.enableSimpleBroker("/topic", "/queue");
        cfg.setUserDestinationPrefix("/user");
    }
}

