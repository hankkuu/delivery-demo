package com.barogo.delivery.auth;

import com.barogo.delivery.jpa.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityBeans {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    // 예시: DB에서 loginId로 조회
    @Bean
    UserDetailsService userDetailsService(MemberRepository memberRepository) {
        return username -> memberRepository.findByLoginId(username)
                .map(m -> User.withUsername(m.getLoginId())
                        .password(m.getPassword()) // 반드시 해시 저장
                        .authorities("ROLE_USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
