package com.barogo.delivery.api.controller;

import com.barogo.delivery.api.controller.model.DeliveryCreateRequest;
import com.barogo.delivery.api.controller.model.DeliverySearchRequest;
import com.barogo.delivery.api.controller.model.DeliverySummary;
import com.barogo.delivery.api.controller.model.DeliveryUpdateDestinationRequest;
import com.barogo.delivery.api.controller.model.DeliveryUpdateStatusRequest;
import com.barogo.delivery.api.service.DeliveryService;
import com.barogo.delivery.auth.CurrentMember;
import com.barogo.delivery.auth.MemberPrincipal;
import com.barogo.delivery.response.ApiResponse;
import com.barogo.delivery.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    // 기간 필수 + 최대 3일 제한
    @GetMapping
    public ResponseEntity<PageResponse<DeliverySummary>> getDeliveries(
            @CurrentMember MemberPrincipal member,
            @Valid @ModelAttribute DeliverySearchRequest request
    ) {
       var results = deliveryService.findByMemberAndPeriod(member.id(), request);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliverySummary>> getDeliveryById(
            @PathVariable Long id,
            @CurrentMember MemberPrincipal member
    ) {
        var results = deliveryService.findById(member.id(), id);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliverySummary>> createDelivery(
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody DeliveryCreateRequest request
    ) {
        var created = deliveryService.createDelivery(member.id(), request);
        var uri = URI.create("/api/deliveries/" + created.orderNumber());
        return ResponseEntity.created(uri).body(ApiResponse.success(created));
    }

    // 도착지 주소 변경 API
    // - path: PATCH /api/deliveries/{deliveryId}/destination
    // - 규칙: 사용자(memberId)의 배달이며, 변경 가능한 상태(예: REQUESTED/ASSIGNED)일 때만 변경
    @PatchMapping("/{deliveryId}/destination")
    public ResponseEntity<ApiResponse<Void>> changeDestination(
            @PathVariable Long deliveryId,
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody DeliveryUpdateDestinationRequest request
    ) {
        deliveryService.changeDestination(member.id(), deliveryId, request.deliveryAddress(), request.deliveryLat(), request.deliveryLng());
        return ResponseEntity.noContent().build();
    }

    // 배달 상태 변경 API
    // - path: PATCH /api/deliveries/{deliveryId}/status
    // - 규칙: 해당 회원의 배달이며, 허용된 상태 전이일 때만 변경(예: REQUESTED->ASSIGNED/PICKED_UP 등)
    @PatchMapping("/{deliveryId}/status")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long deliveryId,
            @CurrentMember MemberPrincipal member,
            @Valid @RequestBody DeliveryUpdateStatusRequest request
    ) {
        deliveryService.changeStatus(member.id(), deliveryId, request.status());
        return ResponseEntity.noContent().build();
    }

}
