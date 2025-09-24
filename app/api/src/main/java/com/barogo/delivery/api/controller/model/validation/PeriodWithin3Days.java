package com.barogo.delivery.api.controller.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PeriodWithin3DaysValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PeriodWithin3Days {
    String message() default "to 는 from 이후여야 하며 최대 3일(72시간)까지만 조회할 수 있습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String from() default "from";
    String to() default "to";
}

