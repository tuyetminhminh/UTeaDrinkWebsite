package net.codejava.utea.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findTop50ByConversation_IdOrderBySentAtDesc(Long conversationId);
    
    /**
     * Đếm số tin nhắn chưa đọc trong conversation, loại trừ tin nhắn của chính senderId (manager)
     */
    long countByConversation_IdAndReadFalseAndSender_IdNot(Long conversationId, Long managerId);
    
    /**
     * Đếm tổng số tin nhắn chưa đọc cho tất cả conversations của manager
     */
    long countByConversation_Admin_IdAndReadFalseAndSender_IdNot(Long managerId, Long sameManagerId);
    
    /**
     * Tìm tất cả tin nhắn chưa đọc trong conversation (không phải của viewerId)
     */
    List<Message> findByConversation_IdAndSender_IdNotAndReadFalse(Long conversationId, Long viewerId);
}