package com.internship.tool.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication
    .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context
    .SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication
    .WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil                jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest  request,
            HttpServletResponse response,
            FilterChain         filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)
                && jwtUtil.validateToken(token)) {
            try {
                // reject refresh tokens used as access tokens
                String type = jwtUtil.extractTokenType(token);
                if (!"access".equals(type)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String      email       =
                    jwtUtil.extractEmail(token);
                UserDetails userDetails =
                    userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isTokenValid(token, email)) {
                    var authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                            .buildDetails(request));
                    SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
                }
            } catch (Exception e) {
                log.error("Cannot set authentication: {}",
                          e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer)
                && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}