package org.acme.resilience;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.ai.AiAssistant;
import org.acme.dto.ChatResponse;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class ResilientAiService {

    @Inject
    AiAssistant aiAssistant;

    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 1, delayUnit = ChronoUnit.SECONDS,
           retryOn = {RuntimeException.class},
           abortOn = {IllegalArgumentException.class})
    @CircuitBreaker(requestVolumeThreshold = 5,
                    failureRatio = 0.6,
                    delay = 15,
                    delayUnit = ChronoUnit.SECONDS,
                    successThreshold = 2)
    @Fallback(fallbackMethod = "chatFallback")
    public ChatResponse chat(String question) {
        return ChatResponse.from(aiAssistant.chat(question));
    }

    public ChatResponse chatFallback(String question) {
        return ChatResponse.fallback("AI service is temporarily unavailable (circuit open or timeout)");
    }

    @Timeout(value = 45, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1, delay = 2, delayUnit = ChronoUnit.SECONDS,
           retryOn = {RuntimeException.class})
    @CircuitBreaker(requestVolumeThreshold = 4,
                    failureRatio = 0.5,
                    delay = 20,
                    delayUnit = ChronoUnit.SECONDS,
                    successThreshold = 2)
    @Fallback(fallbackMethod = "codeReviewFallback")
    public ChatResponse reviewCode(String code) {
        return ChatResponse.from(aiAssistant.reviewCode(code));
    }

    public ChatResponse codeReviewFallback(String code) {
        return ChatResponse.fallback("Code review service is temporarily unavailable");
    }
}
