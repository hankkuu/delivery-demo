package com.barogo.delivery.api.controller;

import com.barogo.delivery.api.controller.model.LoginRequest;
import com.barogo.delivery.api.controller.model.LoginResponse;
import com.barogo.delivery.api.controller.model.SignUpRequest;
import com.barogo.delivery.api.service.MemberService;
import com.barogo.delivery.auth.JwtTokenProvider;
import com.barogo.delivery.domain.Member;
import com.barogo.delivery.response.ApiResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        memberService.signUp(request.loginId(), request.password(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Member m = memberService.authenticate(request.loginId(), request.password());
        Map<String, Object> claims = Map.of(
                "roles", List.of("ROLE_USER"),
                "mid", m.getId(),
                "name", m.getName()
        );

        String accessToken = jwtTokenProvider.createAccessToken(m.getLoginId(), claims);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(ApiResponse.success(new LoginResponse(accessToken)));
    }


}
