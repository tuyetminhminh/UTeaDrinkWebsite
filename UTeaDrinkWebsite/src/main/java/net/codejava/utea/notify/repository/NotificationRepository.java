package net.codejava.utea.notify.repository;

import net.codejava.utea.notify.entity.Notification;
import net.codejava.utea.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByUserOrderByCreatedAtDesc(User user);
}
