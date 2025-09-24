package com.barogo.delivery.api.service;

import com.barogo.delivery.api.controller.model.DeliveryCreateRequest;
import com.barogo.delivery.api.controller.model.DeliverySummary;
import com.barogo.delivery.api.service.validation.DeliveryRequestValidator;
import com.barogo.delivery.domain.Delivery;
import com.barogo.delivery.domain.Member;
import com.barogo.delivery.enums.DeliveryStatus;
import com.barogo.delivery.jpa.DeliveryRepository;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @MockitoBean
    private DeliveryRepository deliveryRepository;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private DeliveryRequestValidator deliveryRequestValidator;

    @Test
    void testCreateDelivery_SuccessfulCreation() {
        Long memberId = 1L;
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "ORDER123",
                "123 Pickup Address",
                37.7749,
                -122.4194,
                "456 Delivery Address",
                37.7749,
                -122.4194,
                "Please deliver ASAP"
        );

        Member mockMember = new Member("loginId", "password", "Test User");
        Delivery mockDelivery = new Delivery(mockMember, "ORDER123", "123 Pickup Address", "456 Delivery Address", DeliveryStatus.REQUESTED, LocalDateTime.now());
        mockDelivery.changeDestination("456 Delivery Address", 37.7749, -122.4194);

        when(memberService.findById(memberId)).thenReturn(mockMember);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(mockDelivery);

        DeliverySummary result = deliveryService.createDelivery(memberId, request);

        assertThat(result).isNotNull();
        assertThat(result.orderNumber()).isEqualTo("ORDER123");

        verify(deliveryRequestValidator, times(1)).validateCreate(request);
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        verify(memberService, times(1)).findById(memberId);
    }

    @Test
    void testCreateDelivery_ValidationError() {
        Long memberId = 1L;
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "ORDER123",
                "123 Pickup Address",
                37.7749,
                -122.4194,
                "456 Delivery Address",
                37.7749,
                -122.4194,
                "Please deliver ASAP"
        );

        doThrow(new ConstraintViolationException("Validation failed", null))
                .when(deliveryRequestValidator).validateCreate(request);

        assertThrows(ConstraintViolationException.class, () -> deliveryService.createDelivery(memberId, request));

        verify(deliveryRequestValidator, times(1)).validateCreate(request);
        verifyNoInteractions(memberService);
        verifyNoInteractions(deliveryRepository);
    }

    @Test
    void testCreateDelivery_DuplicateOrderNumber() {
        Long memberId = 1L;
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "ORDER123",
                "123 Pickup Address",
                37.7749,
                -122.4194,
                "456 Delivery Address",
                37.7749,
                -122.4194,
                "Please deliver ASAP"
        );

        Member mockMember = new Member("loginId", "password", "Test User");
        when(memberService.findById(memberId)).thenReturn(mockMember);
        when(deliveryRepository.save(any(Delivery.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThrows(BusinessException.class, () -> deliveryService.createDelivery(memberId, request));

        verify(deliveryRequestValidator, times(1)).validateCreate(request);
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        verify(memberService, times(1)).findById(memberId);
    }

    @Test
    void testCreateDelivery_MemberNotFound() {
        Long memberId = 1L;
        DeliveryCreateRequest request = new DeliveryCreateRequest(
                "ORDER123",
                "123 Pickup Address",
                37.7749,
                -122.4194,
                "456 Delivery Address",
                37.7749,
                -122.4194,
                "Please deliver ASAP"
        );

        when(memberService.findById(memberId)).thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Member not found"));

        assertThrows(BusinessException.class, () -> deliveryService.createDelivery(memberId, request));

        verify(deliveryRequestValidator, times(1)).validateCreate(request);
        verify(memberService, times(1)).findById(memberId);
        verifyNoInteractions(deliveryRepository);
    }
}