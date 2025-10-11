package net.codejava.utea.repository;

import net.codejava.utea.entity.Conversation;
import net.codejava.utea.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
}