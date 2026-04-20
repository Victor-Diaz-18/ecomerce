package edu.unimagdalena.universitystore.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/not-found")
        public ResponseEntity<Void> notFound() {
            throw new ResourceNotFoundException("Resource not found");
        }

        @GetMapping("/business")
        public ResponseEntity<Void> business() {
            throw new BusinessException("Business rule violated");
        }

        @GetMapping("/conflict")
        public ResponseEntity<Void> conflict() {
            throw new ConflictException("Conflict occurred");
        }

        @GetMapping("/validation")
        public ResponseEntity<Void> validation() {
            throw new ValidationException("Validation failed");
        }
    }

    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"));
    }

    @Test
    void shouldHandleBusinessException() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Business rule violated"));
    }

    @Test
    void shouldHandleConflictException() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict occurred"));
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        mockMvc.perform(get("/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }
}