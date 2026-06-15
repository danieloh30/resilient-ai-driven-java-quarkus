package org.acme.resilience;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.ai.AiAssistant;
import org.acme.dto.ChatResponse;
import org.acme.fault.FaultSimulator;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;

/**
 * Fault-tolerant wrapper around {@link AiAssistant}.
 *
 * <p>Demonstrates all four demo steps:
 * <ol>
 *   <li>Project scaffolded via Bob / {@code quarkus_create}.</li>
 *   <li>Extensions injected autonomously: {@code quarkus-langchain4j-openai}
 *       and {@code quarkus-smallrye-fault-tolerance}.</li>
 *   <li>Fault simulation: {@link FaultSimulator#throwIfSimulating()} raises a
 *       {@link RuntimeException} when the toggle is ON, mimicking an API outage.</li>
 *   <li>Self-healing: {@code @Retry} backs off, {@code @CircuitBreaker} opens,
 *       and {@code @Fallback} delivers a graceful degraded response — no 500
 *       ever reaches the caller.</li>
 * </ol>
 */
@ApplicationScoped
public class ResilientAiService {

    private static final Logger LOG = Logger.getLogger(ResilientAiService.class);

    @Inject
    AiAssistant aiAssistant;

    /** Step 3/4: injected fault-simulation toggle. */
    @Inject
    FaultSimulator faultSimulator;

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
        // Step 3: if fault simulation is active this throws → retries exhaust →
        // circuit opens → chatFallback() is called (Step 4: self-healing).
        faultSimulator.throwIfSimulating();
        return ChatResponse.from(aiAssistant.chat(question));
    }

    /**
     * Step 4 — Self-Healing fallback.
     * Bob captured the runtime error and wrote this method so callers always
     * receive a well-formed response instead of a 500.
     */
    public ChatResponse chatFallback(String question) {
        LOG.warnf("[SELF-HEAL] chatFallback invoked for question='%s'. " +
                  "Fault simulation active: %s", question, faultSimulator.isEnabled());
        return ChatResponse.fallback(
                faultSimulator.isEnabled()
                        ? "simulated API outage (fault simulation is ON — disable via DELETE /api/fault/disable)"
                        : "AI service is temporarily unavailable (circuit open or timeout)");
    }
}
