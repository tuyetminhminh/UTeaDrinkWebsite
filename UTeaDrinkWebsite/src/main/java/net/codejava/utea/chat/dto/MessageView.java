// net/codejava/utea/chat/dto/MessageView.java
package net.codejava.utea.chat.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class MessageView {
    Long id;
    Long conversationId;
    Long senderId;
    String senderName;
    String content;
    String imageUrl;
    LocalDateTime sentAt;
    boolean read;
}
