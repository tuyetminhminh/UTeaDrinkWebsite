package net.codejava.utea.notify.email.repository;

import net.codejava.utea.notify.email.entity.EmailOutbox;
import net.codejava.utea.notify.email.entity.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

	List<EmailOutbox> findTop50ByStatusAndNextRetryAtBeforeOrderByCreatedAtAsc(EmailStatus status, LocalDateTime time);
}
