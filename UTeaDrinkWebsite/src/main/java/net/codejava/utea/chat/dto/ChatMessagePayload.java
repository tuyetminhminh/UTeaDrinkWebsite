// net/codejava/utea/chat/dto/ChatMessagePayload.java
package net.codejava.utea.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)  // ✅ an toàn hơn khi parse
public class ChatMessagePayload {
    private Long conversationId; // nếu null → CUSTOMER sẽ auto tạo với MANAGER
    private String content;
    private String imageUrl; // optional
}
