package net.codejava.utea.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.codejava.utea.ai.config.GeminiConfig;
import net.codejava.utea.ai.dto.GeminiRequest;
import net.codejava.utea.ai.dto.GeminiResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;

    /**
     * Gửi prompt đơn giản đến Gemini
     */
    public String chat(String userMessage) {
        return chat(userMessage, null);
    }

    /**
     * Gửi prompt với system context đến Gemini
     */
    public String chat(String userMessage, String systemContext) {
        try {
            // Build prompt
            String fullPrompt = buildPrompt(userMessage, systemContext);

            // Build request
            GeminiRequest request = GeminiRequest.builder()
                    .contents(List.of(
                            GeminiRequest.Content.builder()
                                    .role("user")
                                    .parts(List.of(
                                            GeminiRequest.Part.builder()
                                                    .text(fullPrompt)
                                                    .build()
                                    ))
                                    .build()
                    ))
                    .generationConfig(GeminiRequest.GenerationConfig.builder()
                            .temperature(0.7)
                            .maxOutputTokens(1024)
                            .topP(0.95)
                            .topK(40)
                            .build())
                    .build();

            // Call API
            String url = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            log.info("🤖 Calling Gemini API...");
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
                    url, entity, GeminiResponse.class);

            if (response.getBody() != null && 
                response.getBody().getCandidates() != null && 
                !response.getBody().getCandidates().isEmpty()) {
                
                GeminiResponse.Candidate candidate = response.getBody().getCandidates().get(0);
                if (candidate.getContent() != null && 
                    candidate.getContent().getParts() != null &&
                    !candidate.getContent().getParts().isEmpty()) {
                    
                    String reply = candidate.getContent().getParts().get(0).getText();
                    log.info("✅ Gemini replied: {} chars", reply.length());
                    return reply;
                }
            }

            log.warn("⚠️ No valid response from Gemini");
            return "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.";

        } catch (Exception e) {
            log.error("❌ Error calling Gemini API: {}", e.getMessage(), e);
            log.error("📍 URL: {}", geminiConfig.getApiUrl());
            log.error("📍 Has API Key: {}", geminiConfig.getApiKey() != null && !geminiConfig.getApiKey().isEmpty());
            log.error("📍 Full error: ", e);
            return "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }

    /**
     * Chat với conversation history
     */
    public String chatWithHistory(List<ConversationMessage> history, String userMessage, String systemContext) {
        try {
            String fullPrompt = buildPrompt(userMessage, systemContext);

            // Build contents with history
            List<GeminiRequest.Content> contents = new ArrayList<>();
            
            // Add history
            for (ConversationMessage msg : history) {
                contents.add(GeminiRequest.Content.builder()
                        .role(msg.getRole())
                        .parts(List.of(GeminiRequest.Part.builder()
                                .text(msg.getText())
                                .build()))
                        .build());
            }

            // Add current user message
            contents.add(GeminiRequest.Content.builder()
                    .role("user")
                    .parts(List.of(GeminiRequest.Part.builder()
                            .text(fullPrompt)
                            .build()))
                    .build());

            GeminiRequest request = GeminiRequest.builder()
                    .contents(contents)
                    .generationConfig(GeminiRequest.GenerationConfig.builder()
                            .temperature(0.7)
                            .maxOutputTokens(1024)
                            .topP(0.95)
                            .topK(40)
                            .build())
                    .build();

            String url = geminiConfig.getApiUrl() + "?key=" + geminiConfig.getApiKey();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            log.info("🤖 Calling Gemini API with history ({} messages)...", history.size());
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
                    url, entity, GeminiResponse.class);

            if (response.getBody() != null && 
                response.getBody().getCandidates() != null && 
                !response.getBody().getCandidates().isEmpty()) {
                
                GeminiResponse.Candidate candidate = response.getBody().getCandidates().get(0);
                if (candidate.getContent() != null && 
                    candidate.getContent().getParts() != null &&
                    !candidate.getContent().getParts().isEmpty()) {
                    
                    String reply = candidate.getContent().getParts().get(0).getText();
                    log.info("✅ Gemini replied: {} chars", reply.length());
                    return reply;
                }
            }

            log.warn("⚠️ No valid response from Gemini");
            return "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.";

        } catch (Exception e) {
            log.error("❌ Error calling Gemini API with history: {}", e.getMessage(), e);
            log.error("📍 URL: {}", geminiConfig.getApiUrl());
            log.error("📍 Has API Key: {}", geminiConfig.getApiKey() != null && !geminiConfig.getApiKey().isEmpty());
            log.error("📍 Full error: ", e);
            return "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau.";
        }
    }

    private String buildPrompt(String userMessage, String systemContext) {
        if (systemContext == null || systemContext.isBlank()) {
            return userMessage;
        }
        return systemContext + "\n\n" + userMessage;
    }

    /**
     * Helper class cho conversation history
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ConversationMessage {
        private String role; // "user" or "model"
        private String text;
    }
}

