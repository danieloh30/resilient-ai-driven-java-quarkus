package org.acme.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface AiAssistant {

    @SystemMessage("""
            You are a helpful assistant for a WeAreDev conference demo about resilient AI-driven Java applications built with Quarkus.
            Be concise, accurate, and helpful. Limit responses to 3-4 sentences unless asked for more detail.
            """)
    @UserMessage("Answer the following question: {question}")
    String chat(String question);

    @SystemMessage("You are a code review assistant. Analyze the provided code and give constructive feedback.")
    @UserMessage("Review this code snippet:\n{code}")
    String reviewCode(String code);
}
