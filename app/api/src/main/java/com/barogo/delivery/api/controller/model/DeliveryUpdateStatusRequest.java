package com.barogo.delivery.api.controller.model;

import com.barogo.delivery.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryUpdateStatusRequest(
        @NotNull
        DeliveryStatus status

) {
}
