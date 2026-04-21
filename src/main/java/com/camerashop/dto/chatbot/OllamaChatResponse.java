package com.camerashop.dto.chatbot;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamaChatResponse {
    private String model;
    private String created_at;
    private OllamaMessage message;
    private boolean done;
}
