package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.chatbot.ChatRequest;
import com.camerashop.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> chatStream(@RequestBody ChatRequest request) {
        StreamingResponseBody stream = outputStream -> {
            try {
                chatbotService.streamChat(request, outputStream);
            } catch (Exception e) {
                try {
                    String err = "{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}\n";
                    outputStream.write(err.getBytes());
                    outputStream.flush();
                } catch (Exception ignored) {}
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(stream);
    }

    @PostMapping("/chat-sync")
    public ResponseEntity<ApiResponse> chatSync(@RequestBody ChatRequest request) {
        try {
            String response = chatbotService.chatNonStream(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Chatbot error: " + e.getMessage()));
        }
    }
}
