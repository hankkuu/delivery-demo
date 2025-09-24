package com.barogo.delivery.api.service;

import com.barogo.delivery.domain.Member;
import com.barogo.delivery.jpa.MemberRepository;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class MemberServiceTest {

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberService memberService;

    /**
     * Tests the MemberService's authenticate method.
     * The method should properly authenticate a user if valid credentials are provided,
     * and throw appropriate exceptions when failure cases occur.
     */

    @Test
    void authenticate_SuccessfulLogin() {
        // Setup test data
        String loginId = "testUser";
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        Member mockMember = new Member(loginId, encodedPassword, "Test User");
        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Execute method and verify
        Member authenticatedMember = memberService.authenticate(loginId, rawPassword);

        assertNotNull(authenticatedMember);
        assertEquals(loginId, authenticatedMember.getLoginId());
        assertEquals("Test User", authenticatedMember.getName());
    }

    @Test
    void authenticate_InvalidLoginId() {
        // Setup
        String loginId = "nonExistentUser";
        String rawPassword = "password123";
        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.empty());

        // Execute and Verify
        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.authenticate(loginId, rawPassword));

        assertEquals(ErrorCode.UNAUTHORIZED_ERROR, exception.getError());
        assertEquals("아이디 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    }

    @Test
    void authenticate_InvalidPassword() {
        // Setup test data
        String loginId = "testUser";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword123";
        Member mockMember = new Member(loginId, encodedPassword, "Test User");
        when(memberRepository.findByLoginId(loginId)).thenReturn(Optional.of(mockMember));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Execute and Verify
        BusinessException exception = assertThrows(BusinessException.class,
                () -> memberService.authenticate(loginId, rawPassword));

        assertEquals(ErrorCode.UNAUTHORIZED_ERROR, exception.getError());
        assertEquals("아이디 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    }
}