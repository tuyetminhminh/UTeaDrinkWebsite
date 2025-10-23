package net.codejava.utea.chat.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String imageUrl; // nếu có ảnh (Cloudinary)
}
