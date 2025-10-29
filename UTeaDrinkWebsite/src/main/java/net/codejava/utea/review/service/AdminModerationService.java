package net.codejava.utea.review.service;

import lombok.RequiredArgsConstructor;
import net.codejava.utea.chat.entity.ChatBan;
import net.codejava.utea.chat.repository.ChatBanRepository;
import net.codejava.utea.common.repository.UserRepository;
import net.codejava.utea.review.entity.Review;
import net.codejava.utea.review.entity.enums.ReviewStatus;
import net.codejava.utea.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminModerationService {

    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;
    private final ChatBanRepository chatBanRepo;

    // === Review Logic ===
    public void approveReview(Long reviewId) {
        Review review = reviewRepo.findById(reviewId).orElseThrow();
        review.setStatus(ReviewStatus.APPROVED);
        reviewRepo.save(review);
    }

    public void rejectReview(Long reviewId) {
        Review review = reviewRepo.findById(reviewId).orElseThrow();
        review.setStatus(ReviewStatus.REJECTED);
        reviewRepo.save(review);
    }

    // === Chat Ban Logic ===
    public void banChat(Long userId, int hours, String reason) {
        // Xóa ban cũ nếu có, để tạo ban mới
        chatBanRepo.findByUserId(userId).ifPresent(chatBanRepo::delete);

        ChatBan ban = ChatBan.builder()
                .user(userRepo.findById(userId).orElseThrow())
                .bannedUntil(LocalDateTime.now().plusHours(hours))
                .reason(reason)
                .build();
        chatBanRepo.save(ban);
    }

    public void unbanChat(Long userId) {
        chatBanRepo.findByUserId(userId).ifPresent(chatBanRepo::delete);
    }
}