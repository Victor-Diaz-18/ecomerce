package edu.unimagdalena.universitystore.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

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