package com.camerashop.dto.chatbot;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OllamaChatRequest {
    private String model;
    private List<OllamaMessage> messages;
    private boolean stream;
    private Map<String, Object> options;
}
