package kz.itzhiti.orderservice.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.itzhiti.orderservice.dto.CreateProductRequest;
import kz.itzhiti.orderservice.dto.UpdateProductRequest;
import kz.itzhiti.orderservice.model.Product;
import kz.itzhiti.orderservice.model.enums.ProductCategory;
import kz.itzhiti.orderservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        testProduct = Product.builder()
                .name("Классический донер")
                .description("Вкусный донер с курицей")
                .price(BigDecimal.valueOf(1500))
                .category(ProductCategory.DONER)
                .available(true)
                .build();
        testProduct = productRepository.save(testProduct);
    }

    @Test
    void getAllProducts_shouldReturnProductList() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", is("Классический донер")))
                .andExpect(jsonPath("$[0].price", is(1500)));
    }

    @Test
    void getAvailableProducts_shouldReturnOnlyAvailableProducts() throws Exception {
        // Создаем недоступный продукт
        Product unavailable = Product.builder()
                .name("Недоступный продукт")
                .price(BigDecimal.valueOf(1000))
                .category(ProductCategory.DONER)
                .available(false)
                .build();
        productRepository.save(unavailable);

        mockMvc.perform(get("/api/products/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    void getProductsByCategory_shouldReturnFilteredProducts() throws Exception {
        Product beverage = Product.builder()
                .name("Coca-Cola")
                .price(BigDecimal.valueOf(300))
                .category(ProductCategory.DRINKS)
                .available(true)
                .build();
        productRepository.save(beverage);

        mockMvc.perform(get("/api/products/category/DONER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("DONER")));
    }

    @Test
    void getProductById_existingProduct_shouldReturnProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Классический донер")));
    }

    @Test
    void getProductById_nonExistingProduct_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/products/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_withAdminRole_shouldCreateSuccessfully() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Острый донер")
                .description("Донер с острым соусом")
                .price(BigDecimal.valueOf(1700))
                .category(ProductCategory.DONER)
                .available(true)
                .build();

        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("preferred_username", "admin");
                            jwt.claim("realm_access", Map.of("roles", List.of("ADMIN")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Острый донер")))
                .andExpect(jsonPath("$.price", is(1700)));
    }

    @Test
    void createProduct_withUserRole_shouldReturn403() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Острый донер")
                .price(BigDecimal.valueOf(1700))
                .category(ProductCategory.DONER)
                .build();

        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("preferred_username", "user");
                            jwt.claim("realm_access", Map.of("roles", List.of("USER")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProduct_withAdminRole_shouldUpdateSuccessfully() throws Exception {
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Обновленный донер")
                .price(BigDecimal.valueOf(1800))
                .available(false)
                .build();

        mockMvc.perform(put("/api/products/{id}", testProduct.getId())
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("preferred_username", "admin");
                            jwt.claim("realm_access", Map.of("roles", List.of("ADMIN")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Обновленный донер")))
                .andExpect(jsonPath("$.price", is(1800)));
    }

    @Test
    void deleteProduct_withAdminRole_shouldDeleteSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", testProduct.getId())
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("preferred_username", "admin");
                            jwt.claim("realm_access", Map.of("roles", List.of("ADMIN")));
                        })))
                .andExpect(status().isNoContent());

        // Проверяем, что продукт удален
        mockMvc.perform(get("/api/products/{id}", testProduct.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_withInvalidData_shouldReturn400() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("") // Пустое имя
                .price(BigDecimal.valueOf(-100)) // Отрицательная цена
                .build();

        mockMvc.perform(post("/api/products")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("preferred_username", "admin");
                            jwt.claim("realm_access", Map.of("roles", List.of("ADMIN")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

