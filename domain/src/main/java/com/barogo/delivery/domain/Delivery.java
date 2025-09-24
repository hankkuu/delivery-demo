package com.barogo.delivery.domain;

import com.barogo.delivery.enums.DeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "delivery",
        indexes = {
                @Index(name = "idx_deliveries_member", columnList = "member_id"),
                @Index(name = "idx_deliveries_member_requested", columnList = "member_id, requested_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 외부 주문번호(비즈니스 유니크)
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    // 배달 번호
    @Column(name = "tracking_number", unique = true, length = 50)
    private String trackingNumber;

    // 픽업지
    @Column(name = "pickup_address", nullable = false, length = 200)
    private String pickupAddress;

    @Column(name = "pickup_lat")
    private Double pickupLat;

    @Column(name = "pickup_lng")
    private Double pickupLng;

    // 배송지
    @Column(name = "delivery_address", nullable = false, length = 200)
    private String deliveryAddress;

    @Column(name = "delivery_lat")
    private Double deliveryLat;

    @Column(name = "delivery_lng")
    private Double deliveryLng;

    // 타임라인(기간 조회에 사용)
    @Column(name = "requested_at", nullable = false, columnDefinition = "DATETIME(0)")
    private LocalDateTime requestedAt;

    @Column(name = "assigned_at", columnDefinition = "DATETIME(0)")
    private LocalDateTime assignedAt;

    @Column(name = "picked_up_at", columnDefinition = "DATETIME(0)")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at", columnDefinition = "DATETIME(0)")
    private LocalDateTime deliveredAt;

    @Column(name = "canceled_at", columnDefinition = "DATETIME(0)")
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeliveryStatus status;

    // 배달 비용 및 거리
    @Column(name = "cost_amount", precision = 12, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "distance_meters")
    private Integer distanceMeters;

    // 라이더(선택)
    @Column(name = "rider_id")
    private Long riderId;

    @Column(name = "memo", length = 500)
    private String memo;

    public Delivery(
            Member member,
            String orderNumber,
            String pickupAddress,
            String deliveryAddress,
            DeliveryStatus status,
            LocalDateTime requestedAt
    ) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    // 상태/타임라인 갱신 편의 메서드(세터 대신)
    public void assignTo(Long riderId, LocalDateTime assignedAt) {
        this.riderId = riderId;
        this.assignedAt = assignedAt;
        this.status = DeliveryStatus.ASSIGNED;
    }

    public void markPickedUp(LocalDateTime pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
        this.status = DeliveryStatus.PICKED_UP;
    }

    public void markDelivered(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
        this.status = DeliveryStatus.DELIVERED;
    }

    public void cancel(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
        this.status = DeliveryStatus.CANCELED;
    }

    // 도착지 주소 변경(허용 상태에서만)
    public void changeDestination(String newAddress, Double newLat, Double newLng) {
        if (!(status == DeliveryStatus.REQUESTED || status == DeliveryStatus.ASSIGNED)) {
            // 상태 전이 규칙 위반 시 예외는 서비스 레이어에서 처리해도 되나,
            // 도메인 일관성 유지를 위해 IllegalStateException으로 방어
            throw new IllegalStateException("현재 상태에서는 도착지 주소를 변경할 수 없습니다: " + status);
        }
        this.deliveryAddress = newAddress;
        this.deliveryLat = newLat;
        this.deliveryLng = newLng;
    }

    // 소유자 규칙(도메인 메서드)
    public boolean isOwnedBy(Long memberId) {
        if (memberId == null) return false;
        return this.member != null && this.member.getId() != null && this.member.getId().equals(memberId);
    }

}
