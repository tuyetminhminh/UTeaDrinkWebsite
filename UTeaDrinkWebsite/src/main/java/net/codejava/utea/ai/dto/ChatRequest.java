package net.codejava.utea.ai.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    private String message;
    private Long sessionId;  // null = tạo session mới
    private String guestSessionId; // cho guest user
}

