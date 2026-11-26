package com.mocktest.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Very simple sliding-window limiter for /api/auth/**: at most 10 requests per minute per IP.
 * This slows down password guessing. Limits: it is in-memory (resets on restart, not shared between
 * several servers) and behind a proxy the IP seen may be the proxy's. A production system would
 * use Redis or an API gateway.
 */
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long now = System.currentTimeMillis();
        Deque<Long> window = hits.computeIfAbsent(request.getRemoteAddr(), k -> new ArrayDeque<>());
        boolean blocked;
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) window.pollFirst();
            blocked = window.size() >= MAX_REQUESTS;
            if (!blocked) window.addLast(now);
        }
        if (blocked) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again in a minute.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
