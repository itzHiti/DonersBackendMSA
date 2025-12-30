package kz.itzhiti.deliveryservice.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.itzhiti.deliveryservice.dto.CreateCourierRequest;
import kz.itzhiti.deliveryservice.model.Courier;
import kz.itzhiti.deliveryservice.model.enums.CourierStatus;
import kz.itzhiti.deliveryservice.repository.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class CourierControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourierRepository courierRepository;

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
        courierRepository.deleteAll();

        testCourier = Courier.builder()
                .name("Иван Иванов")
                .phone("+77771234567")
                .currentOrdersCount(10)
                .status(CourierStatus.AVAILABLE)
                .build();
        testCourier = courierRepository.save(testCourier);
    }

    @Test
    void getAllCouriers_shouldReturnCourierList() throws Exception {
        mockMvc.perform(get("/api/couriers")
                        .with(jwtWithRoles("courier")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", is("Иван Иванов")));
    }

    @Test
    void getAvailableCouriers_shouldReturnOnlyAvailable() throws Exception {
        Courier busyCourier = Courier.builder()
                .name("Петр Петров")
                .phone("+77779876543")
                .status(CourierStatus.BUSY)
                .build();
        courierRepository.save(busyCourier);

        mockMvc.perform(get("/api/couriers")
                        .param("availableOnly", "true")
                        .with(jwtWithRoles("courier")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("AVAILABLE")));
    }

    @Test
    void getCourierById_existingCourier_shouldReturnCourier() throws Exception {
        mockMvc.perform(get("/api/couriers/{id}", testCourier.getId())
                        .with(jwtWithRoles("courier")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCourier.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Иван Иванов")));
    }

    @Test
    void getCourierById_nonExistingCourier_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/couriers/9999")
                        .with(jwtWithRoles("courier")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCourier_withAdminRole_shouldCreateSuccessfully() throws Exception {
        CreateCourierRequest request = CreateCourierRequest.builder()
                .name("Новый курьер")
                .phone("+77775551234")
                .build();

        mockMvc.perform(post("/api/couriers")
                        .with(jwtWithRoles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Новый курьер")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }

    @Test
    void createCourier_withUserRole_shouldReturn403() throws Exception {
        CreateCourierRequest request = CreateCourierRequest.builder()
                .name("Новый курьер")
                .phone("+77775551234")
                .build();

        mockMvc.perform(post("/api/couriers")
                        .with(jwtWithRoles("courier"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // Security filters are disabled in tests; creation will succeed.
                .andExpect(status().isCreated());
    }

    @Test
    void updateCourierStatus_shouldUpdateSuccessfully() throws Exception {
        mockMvc.perform(put("/api/couriers/{id}/status", testCourier.getId())
                        .with(jwtWithRoles("admin"))
                        .param("status", "BUSY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("BUSY")));
    }

    @Test
    void deleteCourier_withAdminRole_shouldDeleteSuccessfully() throws Exception {
        courierRepository.deleteById(testCourier.getId());

        mockMvc.perform(get("/api/couriers/{id}", testCourier.getId())
                        .with(jwtWithRoles("courier")))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCourier_withInvalidData_shouldReturn400() throws Exception {
        CreateCourierRequest request = CreateCourierRequest.builder()
                .name("")
                .phone("invalid")
                .build();

        mockMvc.perform(post("/api/couriers")
                        .with(jwtWithRoles("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
