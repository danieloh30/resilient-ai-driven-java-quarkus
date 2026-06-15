package org.acme.dto;

public class ChatResponse {

    public String answer;
    public String source;

    public ChatResponse(String answer, String source) {
        this.answer = answer;
        this.source = source;
    }

    public static ChatResponse from(String answer) {
        return new ChatResponse(answer, "openai");
    }

    public static ChatResponse fallback(String reason) {
        return new ChatResponse(
                "I'm currently unable to process your request due to: " + reason +
                ". Please try again in a moment.",
                "fallback");
    }
}
