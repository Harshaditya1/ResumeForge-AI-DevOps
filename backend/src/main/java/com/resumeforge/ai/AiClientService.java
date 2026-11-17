package com.resumeforge.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.errors.InternalServerException;
import com.openai.errors.OpenAIIoException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    private static final int MAX_ATTEMPTS = 3;
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String nativeModel;

    public AiClientService(
            ChatClient.Builder chatClientBuilder,
            @Value("${GEMINI_API_KEY}") String apiKey,
            @Value("${GEMINI_NATIVE_MODEL:gemini-2.5-flash}")
            String nativeModel
    ) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.create();
        this.apiKey = apiKey;
        this.nativeModel = nativeModel;
    }

    public <T> T ask(String prompt, Class<T> responseType) {
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return chatClient.prompt()
                        .user(prompt)
                        .call()
                        .entity(responseType);
            } catch (InternalServerException | OpenAIIoException ex) {
                lastFailure = ex;
                if (attempt == MAX_ATTEMPTS) {
                    break;
                }

                try {
                    Thread.sleep(500L * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "AI request retry was interrupted.",
                            interruptedException
                    );
                }
            }
        }

        try {
            return askWithNativeGemini(prompt, responseType);
        } catch (RuntimeException fallbackFailure) {
            if (lastFailure != null) {
                fallbackFailure.addSuppressed(lastFailure);
            }
            throw fallbackFailure;
        }
    }

    private <T> T askWithNativeGemini(
            String prompt,
            Class<T> responseType
    ) {
        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        JsonNode response = restClient.post()
                .uri(UriComponentsBuilder
                        .fromUriString(
                                GEMINI_API_URL + nativeModel + ":generateContent"
                        )
                        .queryParam("key", apiKey)
                        .build()
                        .toUri())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);

        String text = response
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null);

        if (text == null || text.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response."
            );
        }

        try {
            return objectMapper.readValue(
                    removeCodeFence(text),
                    responseType
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Gemini returned an invalid JSON response.",
                    ex
            );
        }
    }

    private String removeCodeFence(String response) {
        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        return cleaned.trim();
    }
}