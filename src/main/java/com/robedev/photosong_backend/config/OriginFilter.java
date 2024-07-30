package com.robedev.photosong_backend.config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class OriginFilter extends OncePerRequestFilter {

    private static final String ALLOWED_ORIGIN = "https://photosong.vercel.app";
    //private static final String ALLOWED_ORIGIN = "http://localhost:4200";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");

        if (ALLOWED_ORIGIN.equals(origin)) {
            // Allow the request to proceed
            filterChain.doFilter(request, response);
        } else {
            // Deny the request
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        }
    }
}