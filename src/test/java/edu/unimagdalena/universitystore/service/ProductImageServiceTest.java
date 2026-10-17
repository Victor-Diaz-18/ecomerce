package edu.unimagdalena.universitystore.service;

import edu.unimagdalena.universitystore.entity.Product;
import edu.unimagdalena.universitystore.entity.ProductImage;
import edu.unimagdalena.universitystore.exception.ResourceNotFoundException;
import edu.unimagdalena.universitystore.exception.ValidationException;
import edu.unimagdalena.universitystore.repository.ProductImageRepository;
import edu.unimagdalena.universitystore.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTest {
    @Mock
    private ProductImageRepository imageRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductImageService service;

    private Product product() {
        Product product = Product.builder().id(5L).name("Camisa").build();
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        return product;
    }

    @Test
    void savesThePhotoAndChangesTheImageVersion() {
        Product product = product();
        when(imageRepository.findByProductId(5L)).thenReturn(Optional.empty());

        service.save(5L, "image/png", new byte[]{1, 2, 3});

        ArgumentCaptor<ProductImage> saved = ArgumentCaptor.forClass(ProductImage.class);
        verify(imageRepository).save(saved.capture());
        assertEquals(5L, saved.getValue().getProductId());
        assertEquals("image/png", saved.getValue().getContentType());
        assertNotNull(product.getImageVersion());
        verify(productRepository).save(product);
    }

    @Test
    void replacingThePhotoReusesTheSameRow() {
        product();
        ProductImage existing = ProductImage.builder().id(9L).productId(5L).contentType("image/jpeg").data(new byte[]{9}).build();
        when(imageRepository.findByProductId(5L)).thenReturn(Optional.of(existing));

        service.save(5L, "image/webp", new byte[]{1});

        assertEquals("image/webp", existing.getContentType());
        verify(imageRepository).save(existing);
    }

    @Test
    void rejectsFilesThatAreNotPhotos() {
        product();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> service.save(5L, "application/pdf", new byte[]{1}));

        assertEquals("La foto debe ser JPG, PNG o WebP", exception.getMessage());
        verify(imageRepository, never()).save(any());
    }

    @Test
    void rejectsPhotosOverTwoMegabytes() {
        product();

        assertThrows(ValidationException.class,
                () -> service.save(5L, "image/jpeg", new byte[(int) ProductImageService.MAX_BYTES + 1]));

        verify(imageRepository, never()).save(any());
    }

    @Test
    void removingThePhotoClearsTheImageVersion() {
        Product product = product();
        product.setImageVersion(123L);
        ProductImage existing = ProductImage.builder().id(9L).productId(5L).build();
        when(imageRepository.findByProductId(5L)).thenReturn(Optional.of(existing));

        service.delete(5L);

        verify(imageRepository).delete(existing);
        assertNull(product.getImageVersion());
    }

    @Test
    void askingForAMissingPhotoAnswersNotFound() {
        when(imageRepository.findByProductId(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.find(5L));
    }
}
