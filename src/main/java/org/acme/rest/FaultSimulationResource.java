package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.fault.FaultSimulator;

import java.util.Map;

/**
 * Demo control plane for <strong>Step 3 – Fault Simulation</strong>.
 *
 * <p>Exposes three endpoints so that a live demo audience can watch the
 * system break and self-heal:
 *
 * <ul>
 *   <li>{@code POST /api/fault/enable}  – activates the simulated crash flag;
 *       every subsequent AI call will throw a {@link RuntimeException}.</li>
 *   <li>{@code DELETE /api/fault/disable} – deactivates the flag; AI calls
 *       return to normal.</li>
 *   <li>{@code GET /api/fault/status}  – reports whether fault simulation is
 *       currently active.</li>
 * </ul>
 *
 * <p><strong>Step 4 – Self-Healing</strong>: once the flag is enabled, call
 * {@code POST /api/ai/chat} several times.  SmallRye Fault Tolerance will
 * retry (with backoff), open the circuit breaker, and ultimately invoke the
 * fallback method — returning a graceful degraded response with
 * {@code "source": "fallback"} instead of a raw 500.
 */
@Path("/api/fault")
@Produces(MediaType.APPLICATION_JSON)
public class FaultSimulationResource {

    @Inject
    FaultSimulator faultSimulator;

    /**
     * Step 3 — Enable fault simulation.
     * All AI service calls will intentionally throw, triggering the
     * Retry → CircuitBreaker → Fallback chain.
     */
    @POST
    @Path("/enable")
    public Map<String, Object> enableFault() {
        faultSimulator.enable();
        return Map.of(
                "faultSimulation", "ENABLED",
                "effect", "AI calls will now throw RuntimeException to simulate an API outage",
                "step", "3 — Fault Simulation active. Call POST /api/ai/chat to observe failures.",
                "selfHeal", "SmallRye Fault Tolerance will retry, open circuit, and invoke fallback automatically"
        );
    }

    /**
     * Restore normal behaviour — AI calls delegate to OpenAI as usual.
     */
    @DELETE
    @Path("/disable")
    public Map<String, Object> disableFault() {
        faultSimulator.disable();
        return Map.of(
                "faultSimulation", "DISABLED",
                "effect", "AI calls restored to normal OpenAI delegation",
                "step", "Circuit Breaker will close after successThreshold healthy responses"
        );
    }

    /**
     * Query whether fault simulation is active.
     */
    @GET
    @Path("/status")
    public Map<String, Object> status() {
        boolean active = faultSimulator.isEnabled();
        return Map.of(
                "faultSimulation", active ? "ENABLED" : "DISABLED",
                "description", active
                        ? "Fault simulation is ON — AI calls will fail and trigger fallback (Step 3/4)"
                        : "Fault simulation is OFF — AI calls operate normally"
        );
    }
}
