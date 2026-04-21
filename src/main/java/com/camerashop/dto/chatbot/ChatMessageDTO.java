package com.camerashop.dto.chatbot;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {
    private String role; // system, user, assistant
    private String content;
}
