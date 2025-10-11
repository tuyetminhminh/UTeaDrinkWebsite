package net.codejava.utea.repository;

import net.codejava.utea.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByAdmin_IdAndCustomer_Id(Long adminId, Long customerId);
    List<Conversation> findByAdmin_Id(Long adminId);
    List<Conversation> findByCustomer_Id(Long customerId);

}
