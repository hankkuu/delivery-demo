package com.barogo.delivery.api.controller.model;

import com.barogo.delivery.domain.Delivery;
import com.barogo.delivery.enums.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliverySummary(
        Long id,
        String orderNumber,
        DeliveryStatus status,
        LocalDateTime requestedAt,
        LocalDateTime deliveredAt,
        String pickupAddress,
        String deliveryAddress
) {
    public static DeliverySummary of(Delivery d) {
        return new DeliverySummary(
                d.getId(),
                d.getOrderNumber(),
                d.getStatus(),
                d.getRequestedAt(),
                d.getDeliveredAt(),
                d.getPickupAddress(),
                d.getDeliveryAddress()
        );
    }
}

