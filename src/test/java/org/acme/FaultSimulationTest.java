package org.acme;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.ai.AiAssistant;
import org.acme.fault.FaultSimulator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests covering the fault-simulation demo (Steps 3 & 4).
 *
 * <ul>
 *   <li>Step 3: {@code POST /api/fault/enable} activates the crash flag.</li>
 *   <li>Step 4: subsequent AI calls return a graceful fallback response
 *               ({@code "source": "fallback"}) — never a 500.</li>
 *   <li>After {@code DELETE /api/fault/disable} the circuit closes and normal
 *       responses resume.</li>
 * </ul>
 */
@QuarkusTest
class FaultSimulationTest {

    @InjectMock
    AiAssistant aiAssistant;

    @Inject
    FaultSimulator faultSimulator;

    @AfterEach
    void resetFaultSimulator() {
        // Always restore clean state between tests.
        faultSimulator.disable();
    }

    // -------------------------------------------------------------------------
    // Step 3: fault/status and fault/enable endpoints
    // -------------------------------------------------------------------------

    @Test
    void testFaultStatusInitiallyDisabled() {
        given()
                .when().get("/api/fault/status")
                .then()
                .statusCode(200)
                .body("faultSimulation", is("DISABLED"));
    }

    @Test
    void testEnableFaultSimulation() {
        given()
                .contentType(ContentType.JSON)
                .when().post("/api/fault/enable")
                .then()
                .statusCode(200)
                .body("faultSimulation", is("ENABLED"));

        given()
                .when().get("/api/fault/status")
                .then()
                .statusCode(200)
                .body("faultSimulation", is("ENABLED"));
    }

    @Test
    void testDisableFaultSimulation() {
        // Enable first
        faultSimulator.enable();

        given()
                .contentType(ContentType.JSON)
                .when().delete("/api/fault/disable")
                .then()
                .statusCode(200)
                .body("faultSimulation", is("DISABLED"));
    }

    // -------------------------------------------------------------------------
    // Step 4: self-healing — fallback fires when fault is active
    // -------------------------------------------------------------------------

    /**
     * With fault simulation enabled, calling {@code POST /api/ai/chat} must
     * still return HTTP 200 with a fallback body — never a 500.
     * This is the "self-healing" behaviour Bob wrote via @Fallback.
     */
    @Test
    void testChatFallbackWhenFaultSimulationEnabled() {
        // Activate Step 3: simulated API outage
        faultSimulator.enable();

        given()
                .contentType(ContentType.JSON)
                .body("{\"question\": \"What is Quarkus?\"}")
                .when().post("/api/ai/chat")
                .then()
                .statusCode(200)
                .body("source", is("fallback"))
                .body("answer", containsString("unable to process"))
                .body("answer", containsString("simulated API outage"));
    }

    /**
     * After disabling fault simulation, successful AI responses resume.
     */
    @Test
    void testNormalResponseAfterFaultDisabled() {
        faultSimulator.enable();
        faultSimulator.disable();

        Mockito.when(aiAssistant.chat("What is resilience?"))
                .thenReturn("Resilience means recovering gracefully from failures.");

        given()
                .contentType(ContentType.JSON)
                .body("{\"question\": \"What is resilience?\"}")
                .when().post("/api/ai/chat")
                .then()
                .statusCode(200)
                .body("source", is("openai"))
                .body("answer", is("Resilience means recovering gracefully from failures."));
    }
}
