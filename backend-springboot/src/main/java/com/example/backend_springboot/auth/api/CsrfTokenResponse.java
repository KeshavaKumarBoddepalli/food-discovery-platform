package com.example.backend_springboot.auth.api;

public record CsrfTokenResponse(String headerName, String token) {
}
