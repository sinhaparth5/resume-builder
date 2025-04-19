package com.example.resume_builder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TextEnhancerService {
    private final WebClient webClient;

    public TextEnhancerService(@Value("${xai.api.token}") String apiToken) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.x.ai/v1")
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String enhanceText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String prompt = "Rewrite for a Harvard-style CV: use strong action verbs (e.g., Developed, Led), avoid personal pronouns, quantify achievements (e.g., 'by 20%'), ensure professional tone. Input: " + text;
        String requestBody = """
            {
                "messages": [
                    {
                        "role": "system",
                        "content": "You are a professional CV enhancement assistant."
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "model": "grok-3-latest",
                "stream": false,
                "temperature": 0
            }
            """.formatted(prompt.replace("\"", "\\\""));

        Mono<String> response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        // Parse the JSON response to extract the content
        String rawResponse = response.block();
        if (rawResponse != null) {
            // Simple JSON parsing to extract "content" from the first choice
            int contentStart = rawResponse.indexOf("\"content\":\"") + 11;
            int contentEnd = rawResponse.indexOf("\"", contentStart);
            if (contentStart > 10 && contentEnd > contentStart) {
                return rawResponse.substring(contentStart, contentEnd).replace("\\n", "\n").trim();
            }
        }
        return text; // Fallback to original text if parsing fails
    }
}
