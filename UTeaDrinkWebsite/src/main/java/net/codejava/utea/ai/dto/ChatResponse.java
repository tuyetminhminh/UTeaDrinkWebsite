package net.codejava.utea.ai.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private Long sessionId;
    private String reply;
    private String error;
    private Boolean success;
}

