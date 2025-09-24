package com.barogo.delivery.api.controller.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;

public class PeriodWithin3DaysValidator implements ConstraintValidator<PeriodWithin3Days, Object> {
    private String fromField;
    private String toField;

    @Override
    public void initialize(PeriodWithin3Days constraintAnnotation) {
        this.fromField = constraintAnnotation.from();
        this.toField = constraintAnnotation.to();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 1) 빈(Null) 객체면 다른 검증에서 처리하도록 통과
        if (value == null) return true;

        // 2) 필드 조회 (없으면 검증 스킵)
        Class<?> clazz = value.getClass();
        Field fromF = ReflectionUtils.findField(clazz, fromField);
        Field toF   = ReflectionUtils.findField(clazz, toField);
        if (fromF == null || toF == null) {
            return true; // 존재하지 않는 필드명은 이 검증의 대상이 아님
        }

        // 3) 접근 가능화 후 값 추출
        ReflectionUtils.makeAccessible(fromF);
        ReflectionUtils.makeAccessible(toF);
        Object fromObj = ReflectionUtils.getField(fromF, value);
        Object toObj   = ReflectionUtils.getField(toF, value);

        // 4) 타입/널 체크: 조건이 안 맞으면 검증 스킵
        if (!(fromObj instanceof LocalDateTime from) || !(toObj instanceof LocalDateTime to)) return true;

        // 5) 실제 규칙: to가 from 이후이고, 차이가 72시간(3일) 이내
        if (!to.isAfter(from)) return false;
        long hours = Duration.between(from, to).toHours();
        return hours <= 72;
    }
}

