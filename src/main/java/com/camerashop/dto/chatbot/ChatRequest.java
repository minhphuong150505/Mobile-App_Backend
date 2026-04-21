package com.camerashop.dto.chatbot;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {
    private List<ChatMessageDTO> messages;
}
