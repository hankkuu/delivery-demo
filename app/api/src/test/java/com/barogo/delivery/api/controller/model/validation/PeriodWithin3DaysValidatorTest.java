package com.barogo.delivery.api.controller.model.validation;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodWithin3DaysValidatorTest {

    @Test
    void isValid_shouldReturnTrueIfFromAndToFieldsAreNotPresent() {
        PeriodWithin3DaysValidator validator = new PeriodWithin3DaysValidator();
        validator.initialize(createAnnotation("nonExistentFrom", "nonExistentTo"));
        TestObject testObject = new TestObject(null, null);
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(testObject, context);

        assertTrue(result);
    }

    @Test
    void isValid_shouldReturnTrueIfFieldsAreNotOfTypeLocalDateTime() {
        PeriodWithin3DaysValidator validator = new PeriodWithin3DaysValidator();
        validator.initialize(createAnnotation("notLocalDateTimeFrom", "notLocalDateTimeTo"));
        TestObjectWithInvalidFields testObject = new TestObjectWithInvalidFields("2022-09-01", "2022-09-02");
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(testObject, context);

        assertTrue(result);
    }

    @Test
    void isValid_shouldReturnTrueIfEitherFromOrToFieldIsNull() {
        PeriodWithin3DaysValidator validator = new PeriodWithin3DaysValidator();
        validator.initialize(createAnnotation("from", "to"));
        TestObject testObject = new TestObject(null, LocalDateTime.now());
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(testObject, context);

        assertTrue(result);
    }

    @Test
    void isValid_shouldReturnFalseIfToFieldIsNotAfterFromField() {
        PeriodWithin3DaysValidator validator = new PeriodWithin3DaysValidator();
        validator.initialize(createAnnotation("from", "to"));
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.minusHours(1); // to is before from
        TestObject testObject = new TestObject(from, to);
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(testObject, context);

        assertFalse(result);
    }

    @Test
    void isValid_shouldReturnFalseIfDurationExceedsThreeDays() {
        PeriodWithin3DaysValidator validator = new PeriodWithin3DaysValidator();
        validator.initialize(createAnnotation("from", "to"));
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(4); // exceeds 3 days
        TestObject testObject = new TestObject(from, to);
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(testObject, context);

        assertFalse(result);
    }

    @Test
    void isValid_shouldReturnTrueIfDurationIsWithinThreeDays() {
        PeriodWithin3DaysValidator validator = new PeriodWithin3DaysValidator();
        validator.initialize(createAnnotation("from", "to"));
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(2).plusHours(23); // within 3 days
        TestObject testObject = new TestObject(from, to);
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(testObject, context);

        assertTrue(result);
    }

    private PeriodWithin3Days createAnnotation(String from, String to) {
        return new PeriodWithin3Days() {
            @Override
            public String message() {
                return "";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public String from() {
                return from;
            }

            @Override
            public String to() {
                return to;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return PeriodWithin3Days.class;
            }
        };
    }

    static class TestObject {
        private LocalDateTime from;
        private LocalDateTime to;

        public TestObject(LocalDateTime from, LocalDateTime to) {
            this.from = from;
            this.to = to;
        }
    }

    static class TestObjectWithInvalidFields {
        private String notLocalDateTimeFrom;
        private String notLocalDateTimeTo;

        public TestObjectWithInvalidFields(String notLocalDateTimeFrom, String notLocalDateTimeTo) {
            this.notLocalDateTimeFrom = notLocalDateTimeFrom;
            this.notLocalDateTimeTo = notLocalDateTimeTo;
        }
    }
}