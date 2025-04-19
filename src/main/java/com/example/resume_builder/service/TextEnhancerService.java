package com.example.resume_builder.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TextEnhancerService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${huggingface.api.token}")
    private String apiToken;

    public TextEnhancerService(WebClient.Builder webClient) {
        this.webClient = webClient.baseUrl("https://api-inference.huggingface.co").build();
        this.objectMapper = new ObjectMapper();
    }

    public String enhanceText(String inputText) {
        if (inputText == null || inputText.trim().isEmpty()) {
            return "";
        }
        String prompt = "Transform the following text into professional CV bullet points in English: " + inputText;
        try {
            String response = webClient.post()
                    .uri("/models/gpt2")
                    .header("Authorization", "Bearer " + apiToken)
                    .bodyValue(new TextInput(prompt))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return extractEnhancedText(response);
        } catch (Exception e) {
            return inputText;
        }
    }

    private String extractEnhancedText(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            // Extract just the generated text from the response
            if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                JsonNode firstResult = jsonNode.get(0);
                if (firstResult.has("generated_text")) {
                    String generatedText = firstResult.get("generated_text").asText();
                    // Extract only the enhanced part after the prompt
                    int promptEndIndex = generatedText.indexOf(": ") + 2;
                    if (promptEndIndex > 2 && promptEndIndex < generatedText.length()) {
                        generatedText = generatedText.substring(promptEndIndex);
                    }
                    return generatedText.trim();
                }
            }
            return response.trim();
        } catch (Exception e) {
            // If JSON parsing fails, just clean up the raw response
            return response.replaceAll("\\[\\{\"[^\"]+\":\"([^\"]+)\"}]", "$1").trim();
        }
    }

    static class TextInput {
        private String inputs;

        public TextInput(String inputs) {
            this.inputs = inputs;
        }

        public String getInputs() {
            return inputs;
        }
    }
}
