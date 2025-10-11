package net.codejava.utea.service;

import net.codejava.utea.entity.Conversation;
import net.codejava.utea.entity.Message;

import java.util.List;

public interface ChatService {
    Conversation getOrCreateConversation(Long adminId, Long customerId);
    List<Message> getMessages(Long conversationId);
    Message saveMessage(Long conversationId, Long senderId, String content);
    List<Conversation> getConversationsByAdmin(Long adminId);
    List<Conversation> getConversationsByCustomer(Long customerId);
}
