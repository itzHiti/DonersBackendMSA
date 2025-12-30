package kz.itzhiti.deliveryservice.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.itzhiti.deliveryservice.dto.UpdateDeliveryStatusRequest;
import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.Delivery;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.model.enums.DeliveryStatus;
import kz.itzhiti.deliveryservice.repository.CourierRepository;
import kz.itzhiti.deliveryservice.repository.DeliveryRepository;
import kz.itzhiti.deliveryservice.service.DeliveryEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeliveryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private CourierRepository courierRepository;

    @MockBean
    private DeliveryEventProducer deliveryEventProducer;

    private Delivery testDelivery;
    private Courier testCourier;

    private JwtRequestPostProcessor jwtWithRoles(String... roles) {
        List<String> rolesList = Arrays.asList(roles);
        List<SimpleGrantedAuthority> auths = rolesList.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                .collect(Collectors.toList());
        return jwt().authorities(auths.toArray(new SimpleGrantedAuthority[0]))
                .jwt(jwt -> jwt.claim("realm_access", Map.of("roles", rolesList)));
    }

    @BeforeEach
    void setUp() {
        deliveryRepository.deleteAll();
        courierRepository.deleteAll();

        testCourier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .status(CourierStatus.AVAILABLE)
                .build();
        testCourier = courierRepository.save(testCourier);

        testDelivery = Delivery.builder()
                .orderId(101L)
                .status(DeliveryStatus.PENDING)
                .address("Алматы, ул. Абая 10")
                .phone("+77779876543")
                .build();
        testDelivery = deliveryRepository.save(testDelivery);

        Mockito.doNothing().when(deliveryEventProducer).sendDeliveryCompletedEvent(Mockito.any());
    }

    @Test
    void getAllDeliveries_shouldReturnDeliveryList() throws Exception {
        mockMvc.perform(get("/api/deliveries/all")
                        .with(jwtWithRoles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].orderId", is(101)));
    }

    @Test
    void getDeliveryById_existingDelivery_shouldReturnDelivery() throws Exception {
        mockMvc.perform(get("/api/deliveries/{id}", testDelivery.getId())
                        .with(jwtWithRoles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testDelivery.getId().intValue())))
                .andExpect(jsonPath("$.orderId", is(101)));
    }

    @Test
    void getDeliveryByOrderId_shouldReturnDelivery() throws Exception {
        mockMvc.perform(get("/api/deliveries/order/{orderId}", testDelivery.getOrderId())
                        .with(jwtWithRoles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is(101)));
    }

    @Test
    void updateDeliveryStatus_shouldUpdateSuccessfully() throws Exception {
        UpdateDeliveryStatusRequest request = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.IN_TRANSIT)
                .build();

        mockMvc.perform(put("/api/deliveries/{id}/status", testDelivery.getId())
                        .with(jwtWithRoles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_TRANSIT")));
    }

    @Test
    void assignCourier_shouldAssignSuccessfully() throws Exception {
        mockMvc.perform(post("/api/deliveries/{id}/assign/{courierId}",
                        testDelivery.getId(), testCourier.getId())
                        .with(jwtWithRoles("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courier.id", is(testCourier.getId().intValue())))
                .andExpect(jsonPath("$.status", is("ASSIGNED")));
    }

    @Test
    void assignCourier_nonExistingCourier_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/deliveries/{id}/assign/{courierId}",
                        testDelivery.getId(), 9999L)
                        .with(jwtWithRoles("admin")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDeliveryStatus_withUserRole_shouldReturn403() throws Exception {
        UpdateDeliveryStatusRequest request = UpdateDeliveryStatusRequest.builder()
                .status(DeliveryStatus.IN_TRANSIT)
                .build();

        mockMvc.perform(put("/api/deliveries/{id}/status", testDelivery.getId())
                        .with(jwtWithRoles("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
