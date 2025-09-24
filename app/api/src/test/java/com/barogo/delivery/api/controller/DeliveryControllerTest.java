package com.barogo.delivery.api.controller;

import com.barogo.delivery.api.WithMockMember;
import com.barogo.delivery.api.controller.model.DeliveryCreateRequest;
import com.barogo.delivery.api.controller.model.DeliverySummary;
import com.barogo.delivery.api.service.DeliveryService;
import com.barogo.delivery.auth.MemberPrincipal;
import com.barogo.delivery.enums.DeliveryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryService deliveryService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests the createDelivery method in the DeliveryController class.
     * This test validates that a delivery is successfully created and the correct response is returned.
     */
    @Test
    @WithMockMember(id = 1L, username = "testUser", authorities = {"ROLE_USER"})
    void createDelivery_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // Arrange
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "ORDER12345",
                "Pickup Address",
                37.7749,
                -122.4194,
                "Delivery Address",
                37.7740,
                -122.4150,
                "Order memo"
        );

        DeliverySummary mockResponse = new DeliverySummary(
                1L, "ORDER12345", DeliveryStatus.REQUESTED,
                LocalDateTime.now(), LocalDateTime.now(),
                request.pickupAddress(), request.deliveryAddress()
        );
        Mockito.when(deliveryService.createDelivery(eq(1L), any(DeliveryCreateRequest.class)))
                .thenReturn(mockResponse);

        MemberPrincipal principal = new MemberPrincipal(1L, "testUser", null);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        // Act & Assert
        mockMvc.perform(post("/api/deliveries")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/deliveries/ORDER12345"))
                .andExpect(jsonPath("$.data.orderNumber").value(mockResponse.orderNumber()));
    }

    /**
     * Tests the createDelivery method in the DeliveryController class.
     * This test validates that the method returns 400 (Bad Request) when the input data is invalid.
     */
    @Test
    @WithMockMember(id = 1L, username = "testUser", authorities = {"ROLE_USER"})
    void createDelivery_ShouldReturn400_WhenRequestIsInvalid() throws Exception {
        // Arrange
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "", // Invalid orderNumber (empty)
                "Pickup Address",
                null,
                null,
                "", // Invalid deliveryAddress (empty)
                null,
                null,
                "Order memo"
        );

        MemberPrincipal principal = new MemberPrincipal(1L, "testUser", null);

        // Act & Assert
        mockMvc.perform(post("/api/deliveries")
                        .principal(principal::getUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests the createDelivery method in the DeliveryController class.
     * This test validates that an internal server error (500) is handled properly.
     */
    @Test
    @WithMockMember(id = 1L, username = "testUser", authorities = {"ROLE_USER"})
    void createDelivery_ShouldReturn500_WhenInternalErrorOccurs() throws Exception {
        // Arrange
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "ORDER12345",
                "Pickup Address",
                37.7749,
                -122.4194,
                "Delivery Address",
                37.7740,
                -122.4150,
                "Order memo"
        );

        Mockito.when(deliveryService.createDelivery(eq(1L), any(DeliveryCreateRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        MemberPrincipal principal = new MemberPrincipal(1L, "testUser", null);

        // Act & Assert
        mockMvc.perform(post("/api/deliveries")
                        .principal(principal::getUsername)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}