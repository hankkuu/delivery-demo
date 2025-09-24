package com.barogo.delivery.api.controller.model;

import com.barogo.delivery.api.controller.model.validation.PeriodWithin3Days;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@PeriodWithin3Days
public record DeliverySearchRequest(
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime from,

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime to,

        @PageableDefault(size = 20)
        Pageable pageable,

        @Min(0)
        int page,

        @Min(1) @Max(200)
        int size
) { }
