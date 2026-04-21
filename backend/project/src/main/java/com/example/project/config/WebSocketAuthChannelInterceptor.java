package com.example.project.config;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.UserService;
import com.example.project.service.auth.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Objects;
import java.util.regex.*;

/**
 * Interceptor for WebSocket authentication and authorization.
 * This interceptor validates JWT tokens during WebSocket connection establishment
 * and ensures that users have the appropriate permissions for subscribing to specific topics.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern BU_TOPIC =
            Pattern.compile("^/topic/bu/([a-zA-Z0-9\\-]+)/projects\\.created$");
    private static final String GLOBAL_PROJECTS_TOPIC = "/topic/projects.created";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) throw new AccessDeniedException("NO_TOKEN");

            String token = authHeader.substring(7);
            String email = jwtTokenUtil.getUsernameFromToken(token);
            String jti = jwtTokenUtil.getJtiFromToken(token);

            UserDetails userDetails = userService.loadUserByUsername(email);
            boolean valid = jwtTokenUtil.validateToken(token, userDetails) && refreshTokenService.isSessionActive(jti);
            if (!valid) throw new AccessDeniedException("INVALID_TOKEN");

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            accessor.setUser(auth);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (!(principal instanceof Authentication auth) || !auth.isAuthenticated()) {
                throw new AccessDeniedException("NOT_AUTHENTICATED");
            }

            String destination = accessor.getDestination();
            if (GLOBAL_PROJECTS_TOPIC.equals(destination)) {
                User adminUser = userRepository.findByEmail(auth.getName());
                if (adminUser == null) throw new AccessDeniedException("USER_NOT_FOUND");
                String adminRole = adminUser.getRole() != null ? adminUser.getRole().getName() : "";
                if ("ADMIN".equalsIgnoreCase(adminRole) || "ROLE_ADMIN".equalsIgnoreCase(adminRole)) {
                    return message;
                }
                throw new AccessDeniedException("DEST_NOT_ALLOWED");
            }

            Matcher m = BU_TOPIC.matcher(destination == null ? "" : destination);
            if (!m.matches()) throw new AccessDeniedException("DEST_NOT_ALLOWED");

            String topicBuId = m.group(1);
            User user = userRepository.findByEmail(auth.getName());
            if (user == null) throw new AccessDeniedException("USER_NOT_FOUND");

            String role = user.getRole() != null ? user.getRole().getName() : "";
            if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) return message;

            String userBu = user.getBusinessUnit() != null ? user.getBusinessUnit().getIdBusinessUnit() : null;
            if ("MANAGER".equalsIgnoreCase(role) && Objects.equals(userBu, topicBuId)) return message;
            if ("ROLE_MANAGER".equalsIgnoreCase(role) && Objects.equals(userBu, topicBuId)) return message;
            throw new AccessDeniedException("BU_NOT_ALLOWED");
        }

        return message;
    }
}
