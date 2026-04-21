package com.camerashop.service;

import com.camerashop.dto.chatbot.*;
import com.camerashop.dto.CategoryDTO;
import com.camerashop.dto.ProductDTO;
import com.camerashop.dto.AssetDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ollama.model:llama3.2}")
    private String ollamaModel;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    // Asset info can be added to system prompt if needed
    // @Autowired private AssetService assetService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cachedSystemPrompt = null;
    private long lastPromptUpdate = 0;
    private static final long CACHE_TTL_MS = 60000; // 1 phút

    public String buildSystemPrompt() {
        long now = System.currentTimeMillis();
        if (cachedSystemPrompt != null && (now - lastPromptUpdate) < CACHE_TTL_MS) {
            return cachedSystemPrompt;
        }

        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            List<ProductDTO> products = productService.getAllProducts(PageRequest.of(0, 10)).getContent();
            // Note: getAllAssets also returns Page if needed; but assetService.getAllAssets(Pageable) might exist

            StringBuilder sb = new StringBuilder();
            sb.append("Bạn là trợ lý ảo của CameraShop - cửa hàng thiết bị camera hàng đầu. Nhiệm vụ của bạn là tư vấn nhanh, gọn, lẹ cho khách hàng bằng tiếng Việt đơn giản, dễ hiểu.\n\n");

            sb.append("THÔNG TIN HỆ THỐNG:\n");

            // Categories
            sb.append("- Danh mục: ");
            sb.append(categories.stream()
                    .map(CategoryDTO::getCategoryName)
                    .collect(Collectors.joining(", ")));
            sb.append("\n");

            // Products
            sb.append("- Sản phẩm nổi bật:\n");
            for (ProductDTO p : products) {
                sb.append(String.format("  + %s (%s) - %,dđ - còn %d chiếc\n",
                        p.getProductName(), p.getBrand(), p.getPrice(), p.getStockQuantity()));
            }

            sb.append("\nDỊCH VỤ:\n");
            sb.append("- Mua thiết bị mới/đã qua sử dụng\n");
            sb.append("- Thuê thiết bị theo ngày\n");

            sb.append("\nCHÍNH SÁCH:\n");
            sb.append("- Bảo hành 12-24 tháng\n");
            sb.append("- Đổi trả trong 7 ngày\n");
            sb.append("- Giao hàng 2-5 ngày, miễn phí đơn >5tr\n");
            sb.append("- Thanh toán: MoMo, COD, trả góp 0% đơn >10tr\n");

            sb.append("\nQUY TẮC TRẢ LỜI:\n");
            sb.append("1. Ngắn gọn, tối đa 2-3 câu\n");
            sb.append("2. Dùng từ ngữ đơn giản, dễ hiểu\n");
            sb.append("3. Ưu tiên tư vấn sản phẩm và dịch vụ của shop\n");
            sb.append("4. Nếu không biết, hướng dẫn khách liên hệ hotline\n");
            sb.append("5. Luôn lịch sự, thân thiện\n");

            cachedSystemPrompt = sb.toString();
            lastPromptUpdate = now;
            return cachedSystemPrompt;
        } catch (Exception e) {
            return buildDefaultSystemPrompt();
        }
    }

    private String buildDefaultSystemPrompt() {
        return "Bạn là trợ lý ảo của CameraShop. Trả lời ngắn gọn, tối đa 2-3 câu, bằng tiếng Việt đơn giản, dễ hiểu.\n\n" +
               "Dịch vụ: mua và thuê thiết bị camera.\n" +
               "Chính sách: bảo hành 12-24 tháng, đổi trả 7 ngày, giao hàng 2-5 ngày, miễn phí ship đơn >5tr.\n" +
               "Thanh toán: MoMo, COD, trả góp 0% đơn >10tr.";
    }

    public void streamChat(ChatRequest request, OutputStream outputStream) throws Exception {
        String systemPrompt = buildSystemPrompt();

        List<OllamaMessage> ollamaMessages = new ArrayList<>();
        ollamaMessages.add(OllamaMessage.builder().role("system").content(systemPrompt).build());

        if (request.getMessages() != null) {
            for (ChatMessageDTO msg : request.getMessages()) {
                ollamaMessages.add(OllamaMessage.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build());
            }
        }

        OllamaChatRequest ollamaRequest = OllamaChatRequest.builder()
                .model(ollamaModel)
                .messages(ollamaMessages)
                .stream(true)
                .options(Map.of(
                        "temperature", 0.7,
                        "num_predict", 256
                ))
                .build();

        URL url = new URL(ollamaBaseUrl + "/api/chat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(ollamaRequest));
            os.flush();
        }

        int status = conn.getResponseCode();
        if (status >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String err = br.lines().collect(Collectors.joining("\n"));
                throw new RuntimeException("Ollama error (" + status + "): " + err);
            }
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = outputStream) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    OllamaChatResponse chunk = objectMapper.readValue(line, OllamaChatResponse.class);
                    if (chunk.getMessage() != null && chunk.getMessage().getContent() != null) {
                        // Forward same format to frontend
                        out.write(line.getBytes(StandardCharsets.UTF_8));
                        out.write("\n".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }
                    if (chunk.isDone()) {
                        break;
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }
        }
    }

    public String chatNonStream(ChatRequest request) throws Exception {
        String systemPrompt = buildSystemPrompt();

        List<OllamaMessage> ollamaMessages = new ArrayList<>();
        ollamaMessages.add(OllamaMessage.builder().role("system").content(systemPrompt).build());

        if (request.getMessages() != null) {
            for (ChatMessageDTO msg : request.getMessages()) {
                ollamaMessages.add(OllamaMessage.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build());
            }
        }

        OllamaChatRequest ollamaRequest = OllamaChatRequest.builder()
                .model(ollamaModel)
                .messages(ollamaMessages)
                .stream(false)
                .options(Map.of(
                        "temperature", 0.7,
                        "num_predict", 256
                ))
                .build();

        URL url = new URL(ollamaBaseUrl + "/api/chat");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(objectMapper.writeValueAsBytes(ollamaRequest));
            os.flush();
        }

        int status = conn.getResponseCode();
        if (status >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String err = br.lines().collect(Collectors.joining("\n"));
                throw new RuntimeException("Ollama error (" + status + "): " + err);
            }
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String responseBody = reader.lines().collect(Collectors.joining("\n"));
            OllamaChatResponse response = objectMapper.readValue(responseBody, OllamaChatResponse.class);
            return response.getMessage() != null ? response.getMessage().getContent() : "";
        }
    }
}
