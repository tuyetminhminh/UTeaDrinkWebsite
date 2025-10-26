package net.codejava.utea.chat.repository;

import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.enums.ConversationScope;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.manager.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

	Optional<Conversation> findByCustomerAndAdminAndShopAndScope(User customer, User admin, Shop shop,
			ConversationScope scope);

	List<Conversation> findByCustomerOrderByLastMessageAtDesc(User customer);

	List<Conversation> findByAdminOrderByLastMessageAtDesc(User admin);

	List<Conversation> findByShopOrderByLastMessageAtDesc(Shop shop);

	Optional<Conversation> findByAdmin_IdAndCustomer_Id(Long adminId, Long customerId);

	java.util.List<Conversation> findByAdmin_IdOrderByLastMessageAtDesc(Long adminId);

	java.util.List<Conversation> findByCustomer_IdOrderByLastMessageAtDesc(Long customerId);

}
