// net/codejava/utea/chat/dto/ConversationView.java
package net.codejava.utea.chat.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ConversationView {
    Long id;
    Long customerId;
    String customerName;
    Long managerId;
    String managerName;
    String scope;      // SYSTEM/SHOP
    Long shopId;       // nullable
    LocalDateTime lastMessageAt;
    String lastSnippet;
    boolean unread;
    @Builder.Default
    int unreadCount = 0;
}
