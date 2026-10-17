package edu.unimagdalena.universitystore.controller;

import edu.unimagdalena.universitystore.entity.ProductImage;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

/**
 * La foto de un producto: cualquiera la puede ver (es parte del catálogo); solo el administrador la cambia.
 */
@RestController
@RequestMapping("/api/v1/products/{id}/image")
@RequiredArgsConstructor
public class ProductImageController {
    private final ProductImageService service;

    // The URL carries the image version, so a cached copy is never stale and can be kept for long
    @GetMapping
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        ProductImage image = service.find(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(image.getData());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            service.save(id, file.getContentType(), file.getBytes());
        } catch (IOException e) {
            throw new ValidationException("No se pudo leer la foto");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
