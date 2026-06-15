package org.acme.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.dto.ChatRequest;
import org.acme.dto.ChatResponse;
import org.acme.resilience.ResilientAiService;

@Path("/api/ai")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AiResource {

    @Inject
    ResilientAiService resilientAiService;

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "Resilient AI Service is running!";
    }

    @POST
    @Path("/chat")
    public ChatResponse chat(@Valid ChatRequest request) {
        return resilientAiService.chat(request.question);
    }
}
