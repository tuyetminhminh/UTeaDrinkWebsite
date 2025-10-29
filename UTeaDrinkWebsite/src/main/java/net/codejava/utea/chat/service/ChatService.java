// net/codejava/utea/chat/service/ChatService.java
package net.codejava.utea.chat.service;

import net.codejava.utea.chat.dto.ConversationView;
import net.codejava.utea.chat.dto.MessageView;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.enums.ConversationScope;
import net.codejava.utea.common.entity.User;

import java.util.List;

public interface ChatService {
    Conversation getOrCreateCustomerToManager(User customer); // scope SYSTEM
    Conversation getOrCreate(User customer, User manager, ConversationScope scope, Long shopId);

    List<ConversationView> listForManager(Long managerId);
    List<ConversationView> listForCustomer(Long customerId);

    List<MessageView> loadLatestMessages(Long conversationId, int limit);

    MessageView sendMessage(Long conversationId, Long senderId, String content, String imageUrl);

    void markRead(Long conversationId, Long viewerId);
    
    /**
     * Lấy tổng số tin nhắn chưa đọc cho manager
     */
    int getTotalUnreadForManager(Long managerId);
}
