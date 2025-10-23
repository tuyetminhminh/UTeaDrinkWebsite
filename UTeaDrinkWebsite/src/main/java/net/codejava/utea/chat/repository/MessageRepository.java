package net.codejava.utea.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findTop50ByConversation_IdOrderBySentAtDesc(Long conversationId);
}