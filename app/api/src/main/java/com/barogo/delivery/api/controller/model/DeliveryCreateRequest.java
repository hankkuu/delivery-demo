package com.barogo.delivery.api.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryCreateRequest(
        @NotBlank
        @Size(max = 50)
        String orderNumber,

        @NotBlank
        @Size(max = 200)
        String pickupAddress,

        Double pickupLat,
        Double pickupLng,

        @NotBlank
        @Size(max = 200)
        String deliveryAddress,

        Double deliveryLat,
        Double deliveryLng,

        @Size(max = 500)
        String memo
) { }

