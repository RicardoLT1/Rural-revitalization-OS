package com.xiangyun.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.ApiResponse;
import com.xiangyun.common.SecurityHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InternalAuthFilter extends OncePerRequestFilter {
    private final InternalAuthProperties properties;
    private final InternalSignatureVerifier verifier;
    private final InternalNonceStore nonceStore;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public InternalAuthFilter(InternalAuthProperties properties,
                              InternalSignatureVerifier verifier,
                              InternalNonceStore nonceStore,
                              ObjectMapper objectMapper) {
        this.properties = properties;
        this.verifier = verifier;
        this.nonceStore = nonceStore;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        return properties.getExcludePaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            verifier.verify(
                    request.getMethod(),
                    pathWithQuery(request),
                    request.getHeader(SecurityHeaders.INTERNAL_TIMESTAMP),
                    request.getHeader(SecurityHeaders.INTERNAL_NONCE),
                    request.getHeader(SecurityHeaders.TRACE_ID),
                    request.getHeader(SecurityHeaders.INTERNAL_SERVICE),
                    "",
                    request.getHeader(SecurityHeaders.INTERNAL_SIGNATURE),
                    nonceStore);
            filterChain.doFilter(request, response);
        } catch (InternalAuthException ex) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.fail(40300, ex.getMessage()));
        }
    }

    private String pathWithQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank() ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }
}
