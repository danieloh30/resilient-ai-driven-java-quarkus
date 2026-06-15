package org.acme.fault;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Application-scoped bean that holds a runtime flag controlling whether
 * fault simulation is active.  When {@code enabled} is {@code true} any
 * call into {@link org.acme.resilience.ResilientAiService} will throw a
 * {@link RuntimeException}, simulating an external-API outage so that the
 * SmallRye Fault-Tolerance machinery (retry → circuit-breaker → fallback)
 * kicks in and self-heals the response.
 *
 * <p>Step 3 of the demo: toggle ON  → observe 500-like failures on the AI calls.
 * <p>Step 4 of the demo: the fallback method in {@code ResilientAiService}
 * catches those failures and returns a graceful degraded response instead of
 * propagating a 500 to the caller.
 */
@ApplicationScoped
public class FaultSimulator {

    private final AtomicBoolean enabled = new AtomicBoolean(false);

    /** Enable fault simulation — every AI call will intentionally fail. */
    public void enable() {
        enabled.set(true);
    }

    /** Disable fault simulation — normal AI calls resume. */
    public void disable() {
        enabled.set(false);
    }

    /** @return {@code true} if fault simulation is currently active. */
    public boolean isEnabled() {
        return enabled.get();
    }

    /**
     * Called by {@link org.acme.resilience.ResilientAiService} before
     * delegating to the real AI.  Throws when simulation is active so that
     * the fault-tolerance annotations (Retry / CircuitBreaker / Fallback)
     * exercise their full chain.
     */
    public void throwIfSimulating() {
        if (enabled.get()) {
            throw new RuntimeException(
                    "[FAULT-SIM] Simulated external API crash — OpenAI endpoint is intentionally disabled");
        }
    }
}
