package kz.itzhiti.orderservice.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.itzhiti.orderservice.dto.CreateOrderRequest;
import kz.itzhiti.orderservice.dto.OrderItemRequest;
import kz.itzhiti.orderservice.model.Product;
import kz.itzhiti.orderservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductRepository productRepository;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        Product product = Product.builder()
                .name("Doner")
                .price(BigDecimal.valueOf(1500))
                .available(true)
                .build();
        product = productRepository.save(product);

        CreateOrderRequest request = CreateOrderRequest.builder()
                .deliveryAddress("Almaty, Abay 10")
                .phone("+77071234567")
                .notes("No onions")
                .items(List.of(
                        OrderItemRequest.builder()
                                .productId(product.getId())
                                .quantity(2)
                                .build()
                ))
                .build();


        mockMvc.perform(post("/api/orders")
                        .with(jwt().jwt(jwt -> {
                            jwt.claim("preferred_username", "test_user");
                            jwt.claim("realm_access", Map.of("roles", List.of("USER")));
                        }))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("test_user"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
