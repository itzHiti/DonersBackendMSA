package kz.itzhiti.orderservice.service;

import kz.itzhiti.orderservice.dto.CreateProductRequest;
import kz.itzhiti.orderservice.dto.ProductDTO;
import kz.itzhiti.orderservice.dto.UpdateProductRequest;
import kz.itzhiti.orderservice.exception.ResourceNotFoundException;
import kz.itzhiti.orderservice.model.Product;
import kz.itzhiti.orderservice.model.enums.ProductCategory;
import kz.itzhiti.orderservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        Product product1 = Product.builder()
                .id(1L)
                .name("Классический донер")
                .price(BigDecimal.valueOf(1500))
                .category(ProductCategory.DONER)
                .available(true)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Coca-Cola")
                .price(BigDecimal.valueOf(300))
                .category(ProductCategory.DRINKS)
                .available(true)
                .build();

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Классический донер", result.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAvailableProducts_shouldReturnOnlyAvailable() {
        Product availableProduct = Product.builder()
                .id(1L)
                .name("Классический донер")
                .price(BigDecimal.valueOf(1500))
                .available(true)
                .build();

        when(productRepository.findByAvailable(true)).thenReturn(List.of(availableProduct));

        List<ProductDTO> result = productService.getAvailableProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getAvailable());
        verify(productRepository).findByAvailable(true);
    }

    @Test
    void getProductsByCategory_shouldReturnFilteredProducts() {
        Product doner = Product.builder()
                .id(1L)
                .name("Классический донер")
                .category(ProductCategory.DONER)
                .available(true)
                .build();

        when(productRepository.findByCategoryAndAvailable(ProductCategory.DONER, true))
                .thenReturn(List.of(doner));

        List<ProductDTO> result = productService.getProductsByCategory(ProductCategory.DONER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ProductCategory.DONER, result.get(0).getCategory());
        verify(productRepository).findByCategoryAndAvailable(ProductCategory.DONER, true);
    }

    @Test
    void getProductById_existingProduct_shouldReturnProduct() {
        Product product = Product.builder()
                .id(1L)
                .name("Классический донер")
                .price(BigDecimal.valueOf(1500))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Классический донер", result.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_nonExistingProduct_shouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
        verify(productRepository).findById(999L);
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Новый донер")
                .price(BigDecimal.valueOf(1800))
                .category(ProductCategory.DONER)
                .available(true)
                .build();

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Новый донер")
                .price(BigDecimal.valueOf(1800))
                .category(ProductCategory.DONER)
                .available(true)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDTO result = productService.createProduct(request);

        assertNotNull(result);
        assertEquals("Новый донер", result.getName());
        assertEquals(BigDecimal.valueOf(1800), result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_existingProduct_shouldReturnUpdatedProduct() {
        Product existingProduct = Product.builder()
                .id(1L)
                .name("Старое название")
                .price(BigDecimal.valueOf(1500))
                .available(true)
                .build();

        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Новое название")
                .price(BigDecimal.valueOf(1800))
                .available(false)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductDTO result = productService.updateProduct(1L, request);

        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_existingProduct_shouldDeleteSuccessfully() {
        Product product = Product.builder()
                .id(1L)
                .name("Донер")
                .build();

        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_nonExistingProduct_shouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(999L));
        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
