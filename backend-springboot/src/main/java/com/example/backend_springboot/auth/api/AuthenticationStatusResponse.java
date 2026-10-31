package com.example.backend_springboot.auth.api;

import com.example.backend_springboot.user.api.UserProfileResponse;

public record AuthenticationStatusResponse(
		boolean authenticated,
		boolean loginConfigured,
		String loginMessage,
		UserProfileResponse user) {
}
