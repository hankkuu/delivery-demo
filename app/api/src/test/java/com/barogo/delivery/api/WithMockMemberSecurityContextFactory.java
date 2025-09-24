package com.barogo.delivery.api;

import com.barogo.delivery.auth.MemberPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

public class WithMockMemberSecurityContextFactory implements WithSecurityContextFactory<WithMockMember> {
    @Override
    public SecurityContext createSecurityContext(WithMockMember ann) {
        var auths = Arrays.stream(ann.authorities()).map(SimpleGrantedAuthority::new).toList();
        var principal = new MemberPrincipal(ann.id(), ann.username(), auths);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, auths);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}

