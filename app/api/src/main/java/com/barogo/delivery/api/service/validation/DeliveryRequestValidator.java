package com.barogo.delivery.api.service.validation;

import com.barogo.delivery.api.controller.model.DeliveryCreateRequest;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class DeliveryRequestValidator {

    public void validateCreate(DeliveryCreateRequest request) {
        ensureNotBlank(request.deliveryAddress(), "도착지 주소는 필수입니다.");
        ensureMaxLength(request.deliveryAddress(), 200, "도착지 주소는 200자 이하여야 합니다.");

        ensureNotBlank(request.pickupAddress(), "픽업지 주소는 필수입니다.");
        ensureMaxLength(request.pickupAddress(), 200, "픽업지 주소는 200자 이하여야 합니다.");

        validateLatLng(request.pickupLat(), request.pickupLng(), "픽업");
        validateLatLng(request.deliveryLat(), request.deliveryLng(), "도착지");
    }

    public void validateDestination(String address, Double lat, Double lng) {
        ensureNotBlank(address, "도착지 주소는 필수입니다.");
        ensureMaxLength(address, 200, "도착지 주소는 200자 이하여야 합니다.");
        validateLatLng(lat, lng, "도착지");
    }

    public void ensureNotNull(Object value, String message) {
        if (value == null) throw new BusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    private void ensureNotBlank(String s, String message) {
        if (s == null || s.isBlank()) throw new BusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    private void ensureMaxLength(String s, int max, String message) {
        if (s != null && s.length() > max) throw new BusinessException(ErrorCode.INVALID_PARAMETER, message);
    }

    private void validateLatLng(Double lat, Double lng, String label) {
        if (lat != null && (lat < -90.0 || lat > 90.0)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, label + " 위도는 -90.0 ~ 90.0 범위여야 합니다.");
        }
        if (lng != null && (lng < -180.0 || lng > 180.0)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, label + " 경도는 -180.0 ~ 180.0 범위여야 합니다.");
        }
    }
}

