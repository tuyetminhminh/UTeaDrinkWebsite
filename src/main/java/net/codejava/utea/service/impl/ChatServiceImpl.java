package net.codejava.utea.service.impl;

import jakarta.transaction.Transactional;
import net.codejava.utea.entity.Account;
import net.codejava.utea.repository.AccountRepository;
import net.codejava.utea.entity.Conversation;
import net.codejava.utea.entity.Message;
import net.codejava.utea.repository.ConversationRepository;
import net.codejava.utea.repository.MessageRepository;
import net.codejava.utea.service.ChatService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;
    private final AccountRepository accRepo;

    public ChatServiceImpl(ConversationRepository convRepo, MessageRepository msgRepo, AccountRepository accRepo) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.accRepo = accRepo;
    }

    @Override
    public Conversation getOrCreateConversation(Long adminId, Long customerId) {
        return convRepo.findByAdmin_IdAndCustomer_Id(adminId, customerId)
                .orElseGet(() -> {
                    var admin = accRepo.findById(adminId).orElseThrow();
                    var cust = accRepo.findById(customerId).orElseThrow();
                    Conversation c = Conversation.builder().admin(admin).customer(cust).build();
                    return convRepo.save(c);
                });
    }

    @Override
    public List<Message> getMessages(Long conversationId) {
        Conversation c = convRepo.findById(conversationId).orElseThrow();
        return msgRepo.findByConversationOrderBySentAtAsc(c);
    }

    @Override
    public Message saveMessage(Long conversationId, Long senderId, String content) {
        Conversation c = convRepo.findById(conversationId).orElseThrow();
        Account sender = accRepo.findById(senderId).orElseThrow();

        Message m = Message.builder()
                .conversation(c)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build();

        // cập nhật lastMessageAt
        c.setLastMessageAt(LocalDateTime.now());
        convRepo.save(c);

        return msgRepo.save(m);
    }
    @Override
    public List<Conversation> getConversationsByAdmin(Long adminId) {
        return convRepo.findAll()
                .stream()
                .filter(c -> c.getAdmin().getId().equals(adminId))
                .toList();
    }

    @Override
    public List<Conversation> getConversationsByCustomer(Long customerId) {
        return convRepo.findAll()
                .stream()
                .filter(c -> c.getCustomer().getId().equals(customerId))
                .toList();
    }

}
