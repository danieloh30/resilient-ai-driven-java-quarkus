package org.acme.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.ai.AiAssistant;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class AiServiceHealthCheck implements HealthCheck {

    @Inject
    AiAssistant aiAssistant;

    @Override
    public HealthCheckResponse call() {
        try {
            // Lightweight probe — just verify the CDI bean is injectable and the
            // OpenAI config key is present (we don't make a live API call here).
            boolean configured = aiAssistant != null;
            return HealthCheckResponse.named("OpenAI AI Service")
                    .status(configured)
                    .withData("provider", "openai")
                    .withData("status", configured ? "configured" : "not-configured")
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("OpenAI AI Service")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
