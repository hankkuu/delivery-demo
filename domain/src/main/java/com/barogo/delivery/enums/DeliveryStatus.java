package com.barogo.delivery.enums;

public enum DeliveryStatus {
    REQUESTED,   // 주문 생성
    ASSIGNED,    // 배차 완료
    PICKED_UP,   // 픽업 완료
    DELIVERED,   // 배송 완료
    CANCELED     // 주문 취소
}

