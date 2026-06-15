package org.acme;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.ai.AiAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class AiResourceTest {

    @InjectMock
    AiAssistant aiAssistant;

    @Test
    void testPing() {
        given()
                .when().get("/api/ai/ping")
                .then()
                .statusCode(200)
                .body(containsString("Resilient AI Service is running"));
    }

    @Test
    void testChatSuccess() {
        Mockito.when(aiAssistant.chat("What is Quarkus?"))
                .thenReturn("Quarkus is a Kubernetes-native Java framework.");

        given()
                .contentType(ContentType.JSON)
                .body("{\"question\": \"What is Quarkus?\"}")
                .when().post("/api/ai/chat")
                .then()
                .statusCode(200)
                .body("answer", is("Quarkus is a Kubernetes-native Java framework."))
                .body("source", is("openai"));
    }

    @Test
    void testChatValidationBlank() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"question\": \"\"}")
                .when().post("/api/ai/chat")
                .then()
                .statusCode(400);
    }

    @Test
    void testChatValidationTooShort() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"question\": \"hi\"}")
                .when().post("/api/ai/chat")
                .then()
                .statusCode(400);
    }

    @Test
    void testChatFallbackOnException() {
        Mockito.when(aiAssistant.chat(Mockito.anyString()))
                .thenThrow(new RuntimeException("OpenAI unavailable"));

        given()
                .contentType(ContentType.JSON)
                .body("{\"question\": \"What is fault tolerance?\"}")
                .when().post("/api/ai/chat")
                .then()
                .statusCode(200)
                .body("answer", containsString("unable to process"))
                .body("source", is("fallback"));
    }

    @Test
    void testHealthLiveness() {
        given()
                .when().get("/q/health/live")
                .then()
                .statusCode(200)
                .body("status", is("UP"));
    }

    @Test
    void testHealthReadiness() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", is("UP"))
                .body("checks.find { it.name == 'OpenAI AI Service' }.status", is("UP"));
    }
}
