package com.barogo.delivery.api.service;

import com.barogo.delivery.domain.Member;
import com.barogo.delivery.jpa.MemberRepository;
import com.barogo.delivery.response.exception.BusinessException;
import com.barogo.delivery.response.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(String loginId, String rawPassword, String name) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EXCEPTION, "이미 사용 중인 로그인ID입니다.");
        }
        String hashed = passwordEncoder.encode(rawPassword);
        memberRepository.save(new Member(loginId, hashed, name));
    }

    @Transactional(readOnly = true)
    public Member authenticate(String loginId, String rawPassword) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_ERROR, "아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ERROR, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return member;
    }

    @Transactional(readOnly = true)
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
