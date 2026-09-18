package com.pdunghh.auth.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pdunghh.auth.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                String role = claims.get("role", String.class);

                jwtService.checkTokenRevoked(claims.getId(), claims.getSubject(), claims.getIssuedAt());

                AuthenticatedUser principal = new AuthenticatedUser(
                        UUID.fromString(claims.getSubject()),
                        claims.get("email", String.class),
                        role);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        token,
                        authorities(role));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                SecurityContextHolder.clearContext();

                // TODO : global exception handler de tra ve ApiError
            }
        }
        filterChain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> authorities(String role) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (StringUtils.hasText(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }
        return authorities;
    }
}
