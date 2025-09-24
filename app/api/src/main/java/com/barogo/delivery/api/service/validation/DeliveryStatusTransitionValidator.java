package com.barogo.delivery.api.service.validation;

import com.barogo.delivery.enums.DeliveryStatus;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class DeliveryStatusTransitionValidator {

    public void validateTransition(DeliveryStatus current, DeliveryStatus target) {
        if (target == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "변경할 상태는 필수입니다.");
        }
        switch (target) {
            case null -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "변경할 상태는 필수입니다.");
            case ASSIGNED -> {
                if (current != DeliveryStatus.REQUESTED) {
                    reject(current, target);
                }
            }
            case PICKED_UP -> {
                if (current != DeliveryStatus.ASSIGNED) {
                    reject(current, target);
                }
            }
            case DELIVERED -> {
                if (current != DeliveryStatus.PICKED_UP) {
                    reject(current, target);
                }
            }
            case CANCELED -> {
                if (!(current == DeliveryStatus.REQUESTED || current == DeliveryStatus.ASSIGNED)) {
                    reject(current, target);
                }
            }
            case REQUESTED -> throw new BusinessException(ErrorCode.ILLEGAL_STATUS, "REQUESTED 상태로 되돌릴 수 없습니다.");
            default -> throw new BusinessException(ErrorCode.INVALID_PARAMETER, "알 수 없는 상태입니다: " + target);
        }
    }

    private void reject(DeliveryStatus current, DeliveryStatus target) {
        throw new BusinessException(
                ErrorCode.ILLEGAL_STATUS,
                "해당 상태로 변경할 수 없습니다: " + current + " -> " + target
        );
    }
}

