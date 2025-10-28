//// net/codejava/utea/chat/service/impl/ChatServiceImpl.java
//package net.codejava.utea.chat.service.impl;
//
//import lombok.RequiredArgsConstructor;
//import net.codejava.utea.chat.dto.ConversationView;
//import net.codejava.utea.chat.dto.MessageView;
//import net.codejava.utea.chat.entity.Conversation;
//import net.codejava.utea.chat.entity.Message;
//import net.codejava.utea.chat.entity.enums.ConversationScope;
//import net.codejava.utea.chat.repository.ConversationRepository;
//import net.codejava.utea.chat.repository.MessageRepository;
//import net.codejava.utea.common.entity.User;
//import net.codejava.utea.common.repository.UserRepository;
//import net.codejava.utea.manager.entity.Shop;
//import net.codejava.utea.manager.repository.ShopManagerRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Comparator;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class ChatServiceImpl implements net.codejava.utea.chat.service.ChatService {
//
//    private final ConversationRepository conversationRepo;
//    private final MessageRepository messageRepo;
//    private final UserRepository userRepo;
//    private final ShopManagerRepository shopManagerRepo;
//
//    @Override
//    public Conversation getOrCreateCustomerToManager(User customer) {
//        var manager = userRepo.findFirstByRoles_CodeOrderByIdAsc("MANAGER")
//                .orElseThrow(() -> new IllegalStateException("Chưa có MANAGER trong hệ thống"));
//        return getOrCreate(customer, manager, ConversationScope.SYSTEM, null);
//    }
//
//    @Override
//    public Conversation getOrCreate(User customer, User manager, ConversationScope scope, Long shopId) {
//        Shop shop = null;
//        if (scope == ConversationScope.SHOP && shopId != null) {
//            shop = new Shop();
//            shop.setId(shopId);
//        }
//        var found = conversationRepo.findByCustomerAndAdminAndShopAndScope(customer, manager, shop, scope);
//        if (found.isPresent()) return found.get();
//
//        var conv = Conversation.builder()
//                .customer(customer)
//                .admin(manager)
//                .scope(scope)
//                .shop(shop)
//                .createdAt(LocalDateTime.now())
//                .lastMessageAt(LocalDateTime.now())
//                .build();
//        return conversationRepo.save(conv);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ConversationView> listForManager(Long managerId) {
//        return conversationRepo.findByAdmin_IdOrderByLastMessageAtDesc(managerId)
//                .stream().map(c -> {
//                    // Đếm số tin nhắn chưa đọc từ customer (không phải từ manager)
//                    long unreadCount = messageRepo.countByConversation_IdAndReadFalseAndSender_IdNot(c.getId(), managerId);
//                    return ConversationView.builder()
//                            .id(c.getId())
//                            .customerId(c.getCustomer().getId())
//                            .customerName(c.getCustomer().getFullName())
//                            .managerId(c.getAdmin().getId())
//                            .managerName(c.getAdmin().getFullName())
//                            .scope(c.getScope().name())
//                            .shopId(c.getShop() == null ? null : c.getShop().getId())
//                            .lastMessageAt(c.getLastMessageAt())
//                            .lastSnippet(lastSnippet(c.getId()))
//                            .unread(unreadCount > 0)
//                            .unreadCount((int) unreadCount)
//                            .build();
//                }).toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ConversationView> listForCustomer(Long customerId) {
//        return conversationRepo.findByCustomer_IdOrderByLastMessageAtDesc(customerId)
//                .stream().map(c -> ConversationView.builder()
//                        .id(c.getId())
//                        .customerId(c.getCustomer().getId())
//                        .customerName(c.getCustomer().getFullName())
//                        .managerId(c.getAdmin().getId())
//                        .managerName(c.getAdmin().getFullName())
//                        .scope(c.getScope().name())
//                        .shopId(c.getShop() == null ? null : c.getShop().getId())
//                        .lastMessageAt(c.getLastMessageAt())
//                        .lastSnippet(lastSnippet(c.getId()))
//                        .unread(false)
//                        .build()
//                ).toList();
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<MessageView> loadLatestMessages(Long conversationId, int limit) {
//        var list = messageRepo.findTop50ByConversation_IdOrderBySentAtDesc(conversationId);
//        var ordered = list.stream().sorted(Comparator.comparing(Message::getSentAt)).toList();
//        return ordered.stream().map(this::toView).toList();
//    }
//
//    @Override
//    public MessageView sendMessage(Long conversationId, Long senderId, String content, String imageUrl) {
//        var conv = conversationRepo.findById(conversationId).orElseThrow();
//        var sender = userRepo.findById(senderId).orElseThrow();
//
//        // Nếu người gửi là admin/manager của conversation này, đánh dấu đã đọc ngay
//        boolean isAdmin = conv.getAdmin().getId().equals(senderId);
//
//        var msg = Message.builder()
//                .conversation(conv)
//                .sender(sender)
//                .content(content)
//                .imageUrl(imageUrl)
//                .sentAt(LocalDateTime.now())
//                .read(isAdmin) // Manager gửi → read = true, Customer gửi → read = false
//                .build();
//        messageRepo.save(msg);
//
//        conv.setLastMessageAt(msg.getSentAt());
//        conversationRepo.save(conv);
//        return toView(msg);
//    }
//
//    @Override
//    @Transactional
//    public void markRead(Long conversationId, Long viewerId) {
//        // Đánh dấu tất cả tin nhắn trong conversation (không phải của viewerId) là đã đọc
//        var messages = messageRepo.findByConversation_IdAndSender_IdNotAndReadFalse(conversationId, viewerId);
//        messages.forEach(m -> m.setRead(true));
//        messageRepo.saveAll(messages);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public int getTotalUnreadForManager(Long managerId) {
//        long count = messageRepo.countByConversation_Admin_IdAndReadFalseAndSender_IdNot(managerId, managerId);
//        return (int) count;
//    }
//
//    private String lastSnippet(Long conversationId) {
//        var list = messageRepo.findTop50ByConversation_IdOrderBySentAtDesc(conversationId);
//        return list.isEmpty() ? "" : list.get(0).getContent();
//    }
//
//    private MessageView toView(Message m) {
//        return MessageView.builder()
//                .id(m.getId())
//                .conversationId(m.getConversation().getId())
//                .senderId(m.getSender().getId())
//                .senderName(m.getSender().getFullName())
//                .content(m.getContent())
//                .imageUrl(m.getImageUrl())
//                .sentAt(m.getSentAt())
//                .read(m.isRead())
//                .build();
//    }
//}

package net.codejava.utea.chat.service.impl;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.dto.ConversationView;
import net.codejava.utea.chat.dto.MessageView;
import net.codejava.utea.chat.entity.Conversation;
import net.codejava.utea.chat.entity.Message;
import net.codejava.utea.chat.entity.enums.ConversationScope;
import net.codejava.utea.chat.repository.ChatBanRepository;
import net.codejava.utea.chat.repository.ConversationRepository;
import net.codejava.utea.chat.repository.MessageRepository;
import net.codejava.utea.common.entity.User;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.manager.entity.Shop;
import net.codejava.utea.manager.repository.ShopManagerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements net.codejava.utea.chat.service.ChatService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final ShopManagerRepository shopManagerRepo;
    private final ChatBanRepository chatBanRepo;

    @Override
    public Conversation getOrCreateCustomerToManager(User customer) {
        var manager = userRepo.findFirstByRoles_CodeOrderByIdAsc("MANAGER")
                .orElseThrow(() -> new IllegalStateException("Chưa có MANAGER trong hệ thống"));
        return getOrCreate(customer, manager, ConversationScope.SYSTEM, null);
    }

    @Override
    public Conversation getOrCreate(User customer, User manager, ConversationScope scope, Long shopId) {
        Shop shop = null;
        if (scope == ConversationScope.SHOP && shopId != null) {
            shop = new Shop();
            shop.setId(shopId);
        }
        var found = conversationRepo.findByCustomerAndAdminAndShopAndScope(customer, manager, shop, scope);
        if (found.isPresent()) return found.get();

        var conv = Conversation.builder()
                .customer(customer)
                .admin(manager)
                .scope(scope)
                .shop(shop)
                .createdAt(LocalDateTime.now())
                .lastMessageAt(LocalDateTime.now())
                .build();
        return conversationRepo.save(conv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationView> listForManager(Long managerId) {
        return conversationRepo.findByAdmin_IdOrderByLastMessageAtDesc(managerId)
                .stream().map(c -> {
                    // Đếm số tin nhắn chưa đọc từ customer (không phải từ manager)
                    long unreadCount = messageRepo.countByConversation_IdAndReadFalseAndSender_IdNot(c.getId(), managerId);
                    return ConversationView.builder()
                            .id(c.getId())
                            .customerId(c.getCustomer().getId())
                            .customerName(c.getCustomer().getFullName())
                            .managerId(c.getAdmin().getId())
                            .managerName(c.getAdmin().getFullName())
                            .scope(c.getScope().name())
                            .shopId(c.getShop() == null ? null : c.getShop().getId())
                            .lastMessageAt(c.getLastMessageAt())
                            .lastSnippet(lastSnippet(c.getId()))
                            .unread(unreadCount > 0)
                            .unreadCount((int) unreadCount)
                            .build();
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationView> listForCustomer(Long customerId) {
        return conversationRepo.findByCustomer_IdOrderByLastMessageAtDesc(customerId)
                .stream().map(c -> ConversationView.builder()
                        .id(c.getId())
                        .customerId(c.getCustomer().getId())
                        .customerName(c.getCustomer().getFullName())
                        .managerId(c.getAdmin().getId())
                        .managerName(c.getAdmin().getFullName())
                        .scope(c.getScope().name())
                        .shopId(c.getShop() == null ? null : c.getShop().getId())
                        .lastMessageAt(c.getLastMessageAt())
                        .lastSnippet(lastSnippet(c.getId()))
                        .unread(false)
                        .build()
                ).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageView> loadLatestMessages(Long conversationId, int limit) {
        var list = messageRepo.findTop50ByConversation_IdOrderBySentAtDesc(conversationId);
        var ordered = list.stream().sorted(Comparator.comparing(Message::getSentAt)).toList();
        return ordered.stream().map(this::toView).toList();
    }

    @Override
    public MessageView sendMessage(Long conversationId, Long senderId, String content, String imageUrl) {

        var activeBan = chatBanRepo.findByUserIdAndBannedUntilAfter(senderId, LocalDateTime.now());
        if (activeBan.isPresent()) {
            throw new IllegalStateException("Chức năng chat của bạn đã bị tạm khóa.");
        }

        var conv = conversationRepo.findById(conversationId).orElseThrow();
        var sender = userRepo.findById(senderId).orElseThrow();

        // Nếu người gửi là admin/manager của conversation này, đánh dấu đã đọc ngay
        boolean isAdmin = conv.getAdmin().getId().equals(senderId);

        var msg = Message.builder()
                .conversation(conv)
                .sender(sender)
                .content(content)
                .imageUrl(imageUrl)
                .sentAt(LocalDateTime.now())
                .read(isAdmin) // Manager gửi → read = true, Customer gửi → read = false
                .build();
        messageRepo.save(msg);

        conv.setLastMessageAt(msg.getSentAt());
        conversationRepo.save(conv);
        return toView(msg);
    }

    @Override
    @Transactional
    public void markRead(Long conversationId, Long viewerId) {
        // Đánh dấu tất cả tin nhắn trong conversation (không phải của viewerId) là đã đọc
        var messages = messageRepo.findByConversation_IdAndSender_IdNotAndReadFalse(conversationId, viewerId);
        messages.forEach(m -> m.setRead(true));
        messageRepo.saveAll(messages);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalUnreadForManager(Long managerId) {
        long count = messageRepo.countByConversation_Admin_IdAndReadFalseAndSender_IdNot(managerId, managerId);
        return (int) count;
    }

    private String lastSnippet(Long conversationId) {
        var list = messageRepo.findTop50ByConversation_IdOrderBySentAtDesc(conversationId);
        return list.isEmpty() ? "" : list.get(0).getContent();
    }

    private MessageView toView(Message m) {
        return MessageView.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .content(m.getContent())
                .imageUrl(m.getImageUrl())
                .sentAt(m.getSentAt())
                .read(m.isRead())
                .build();
    }
}

