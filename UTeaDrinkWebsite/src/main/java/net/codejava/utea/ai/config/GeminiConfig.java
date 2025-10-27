package net.codejava.utea.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Configuration
@Slf4j
public class GeminiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;
    
    @PostConstruct
    public void logConfig() {
        log.info("🔧 GEMINI CONFIG LOADED:");
        log.info("📍 API URL: {}", apiUrl);
        log.info("🔑 API Key: {}...", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) : "NULL");
        log.info("✅ UTF-8 Encoding enabled for API requests");
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // Đảm bảo UTF-8 cho tiếng Việt
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}

