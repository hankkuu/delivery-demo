package com.barogo.delivery.api.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryUpdateDestinationRequest(
        @NotBlank
        @Size(max = 200)
        String deliveryAddress,
        Double deliveryLat,
        Double deliveryLng
) {
}
