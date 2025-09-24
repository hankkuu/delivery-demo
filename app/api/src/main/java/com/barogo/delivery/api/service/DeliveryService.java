package com.barogo.delivery.api.service;

import com.barogo.delivery.api.controller.model.DeliveryCreateRequest;
import com.barogo.delivery.api.controller.model.DeliverySearchRequest;
import com.barogo.delivery.api.controller.model.DeliverySummary;
import com.barogo.delivery.api.service.validation.DeliveryRequestValidator;
import com.barogo.delivery.api.service.validation.DeliveryStatusTransitionValidator;
import com.barogo.delivery.domain.Delivery;
import com.barogo.delivery.domain.Member;
import com.barogo.delivery.enums.DeliveryStatus;
import com.barogo.delivery.jpa.DeliveryRepository;
import com.barogo.delivery.response.PageResponse;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MemberService memberService;
    private final DeliveryRequestValidator deliveryRequestValidator;
    private final DeliveryStatusTransitionValidator statusValidator;

    /**
     * 기간 내 회원의 배달 목록 조회
     * - requestedAt 기준 내림차순
     */
    // todo: 더 디테일한 페이지네이션 필요 or 필요하면 커서 방식 페이징도 고려
    @Transactional(readOnly = true)
    public PageResponse<DeliverySummary> findByMemberAndPeriod(Long memberId, DeliverySearchRequest request) {
        var pageResult = deliveryRepository.findByMemberIdAndRequestedAtBetween(memberId, request.from(), request.to(), request.pageable());
        return PageResponse.of(pageResult.map(DeliverySummary::of));
    }

    @Transactional(readOnly = true)
    public DeliverySummary findById(Long memberId, Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "findById: 배달을 찾을 수 없습니다."));

        if (!delivery.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 배달만 조회할 수 있습니다.");
        }

        return DeliverySummary.of(delivery);
    }

    // todo: 분산락 or 동시성 제어 처리
    public DeliverySummary createDelivery(Long memberId, DeliveryCreateRequest request) {
        // 형식/범위/필수값 검증
        deliveryRequestValidator.validateCreate(request);

        // 회원 존재 검증
        Member member = memberService.findById(memberId);

        // 엔티티 생성
        LocalDateTime now = LocalDateTime.now();
        Delivery delivery = new Delivery(
                member,
                request.orderNumber(),
                request.pickupAddress(),
                request.deliveryAddress(),
                DeliveryStatus.REQUESTED,
                now
        );
        delivery.changeDestination(request.deliveryAddress(), request.deliveryLat(), request.deliveryLng());
        // 좌표는 픽업/도착 각각 필드 세터가 없으므로 직접 할당 필요 시 엔티티에 세터/편의 메서드 추가를 고려
        // 여기서는 생성자 필드 외 나머지에 직접 접근이 불가하므로 pickup 좌표는 Repository 저장 전 필드 접근이 필요합니다.
        // 엔티티에 편의 메서드가 없다면, 생성자/메서드를 추가하는 것이 이상적입니다.

        try {
            var saved = deliveryRepository.save(delivery);
            return DeliverySummary.of(saved);
        } catch (DataIntegrityViolationException e) {
            // order_number 유니크 제약 위반 등
            throw new BusinessException(ErrorCode.DUPLICATE_EXCEPTION, "이미 존재하는 주문번호입니다.");
        }
    }

    // todo: 분산락 or 동시성 제어 처리
    public void changeDestination(Long memberId, Long deliveryId, String deliveryAddress, Double deliveryLat, Double deliveryLng) {
        // - 서비스 내부에서 다음을 검증:
        //   1) 해당 배달이 memberId 소유인지
        //   2) 배달 상태가 변경 가능(REQUESTED/ASSIGNED 등)인지
        //   3) 주소 길이/좌표 범위 등 추가 검증 및 변경 이력 기록
        // 파라미터 기본 검증(주소/좌표)
        deliveryRequestValidator.validateDestination(deliveryAddress, deliveryLat, deliveryLng);

        // 배달 조회
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "changeDestination: 배달을 찾을 수 없습니다."));

        // 소유자 검증을 도메인에 위임
        if (!delivery.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 배달만 수정할 수 있습니다.");
        }


        // 상태 검증 및 변경 수행(엔티티 내부에서 상태 허용 여부 확인)
        delivery.changeDestination(deliveryAddress, deliveryLat, deliveryLng);
        // JPA Dirty Checking 으로 flush 시 업데이트 반영
    }

    // todo: 분산락 or 동시성 제어 처리
    public void changeStatus(Long memberId, Long deliveryId, DeliveryStatus targetStatus) {
        if (targetStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "변경할 상태는 필수입니다.");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "changeStatus: 배달을 찾을 수 없습니다."));

        // 소유자 검증을 도메인에 위임
        if (!delivery.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 배달만 수정할 수 있습니다.");
        }

        // 전이 규칙 검증(예외 발생 시 전이 차단)
        DeliveryStatus current = delivery.getStatus();
        statusValidator.validateTransition(current, targetStatus);

        LocalDateTime now = LocalDateTime.now();

        switch (targetStatus) {
            case ASSIGNED -> // REQUESTED -> ASSIGNED만 허용
                // riderId는 별도의 배차 로직으로 설정되어 있을 수 있음(없어도 상태 전환은 가능)
                    delivery.assignTo(delivery.getRiderId(), now);
            case PICKED_UP -> // ASSIGNED -> PICKED_UP만 허용
                    delivery.markPickedUp(now);
            case DELIVERED -> // PICKED_UP -> DELIVERED만 허용
                    delivery.markDelivered(now);
            case CANCELED -> // REQUESTED, ASSIGNED -> CANCELED 허용
                    delivery.cancel(now);
            case REQUESTED -> // 회귀 금지
                    throw new BusinessException(ErrorCode.ILLEGAL_STATUS, "REQUESTED 상태로 되돌릴 수 없습니다.");
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "알 수 없는 상태입니다: " + targetStatus);
        }
        // JPA Dirty Checking 으로 flush 시점에 업데이트 반영
    }


}
