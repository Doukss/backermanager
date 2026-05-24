package com.immo.common.security;

import com.immo.auth.repository.UserRepository;
import com.immo.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parse(header.substring(BEARER.length()));
            String userId = claims.getSubject();
            String tenantId = claims.get("tenantId", String.class);
            String role = claims.get("role", String.class);

            boolean active = userRepository.findById(UUID.fromString(userId))
                    .map(user -> user.isActive())
                    .orElse(false);
            if (!active) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Utilisateur suspendu");
                return;
            }

            var authority = new SimpleGrantedAuthority("ROLE_" + role);
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(new JwtHeaderRequest(request, userId, tenantId, role), response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        }
    }

    private static final class JwtHeaderRequest extends HttpServletRequestWrapper {
        private final Map<String, String> jwtHeaders = new LinkedHashMap<>();

        private JwtHeaderRequest(HttpServletRequest request, String userId, String tenantId, String role) {
            super(request);
            jwtHeaders.put("X-User-Id", userId);
            jwtHeaders.put("X-Tenant-ID", tenantId);
            jwtHeaders.put("X-Role", role);
        }

        @Override
        public String getHeader(String name) {
            return jwtHeaders.getOrDefault(name, super.getHeader(name));
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String value = jwtHeaders.get(name);
            if (value != null) {
                return Collections.enumeration(List.of(value));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Collection<String> names = Collections.list(super.getHeaderNames());
            names.addAll(jwtHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
