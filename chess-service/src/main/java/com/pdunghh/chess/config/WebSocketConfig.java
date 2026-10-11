package com.pdunghh.chess.config;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.jwt.secret:3eb08fe65bcca387a304ab725a3fe273641d98f39fda6b0dae2aaa53bf92d9ee}")
    private String jwtSecret;

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Cho phép kết nối WebSocket từ mọi domain (FE dev: 3000, 5173...)
        registry.addEndpoint("/ws-chess")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws-chess")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // Prefix cho các message client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix cho server gửi broadcast (/topic) hoặc gửi riêng cho user (/queue)
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix cho tin nhắn gửi trực tiếp tới từng user cá nhân
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    log.info("WebSocket CONNECT auth header: {}", authHeader);

                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        Claims claims = parseToken(token);
                        if (claims != null) {
                            String userId = claims.getSubject();
                            Principal principal = () -> userId;
                            accessor.setUser(principal);
                            log.info("WebSocket connected successfully for userId: {}", userId);
                        } else {
                            log.warn("Invalid JWT in WebSocket connection, rejecting user principal");
                        }
                    }
                }
                return message;
            }
        });
    }

    private Claims parseToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("WebSocket JWT validation failed: {}", e.getMessage());
            return null;
        }
    }
}
