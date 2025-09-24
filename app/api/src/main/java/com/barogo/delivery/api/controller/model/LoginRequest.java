package com.barogo.delivery.api.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(max = 50)
        String loginId,

        @NotBlank
        @Size(min = 12, max = 255)
        String password
) {}

