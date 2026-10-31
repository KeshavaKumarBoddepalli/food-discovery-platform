package com.example.backend_springboot.auth.api;

import com.example.backend_springboot.user.api.UserProfileResponse;
import com.example.backend_springboot.user.infrastructure.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

	private final AppUserRepository userRepository;
	private final boolean googleLoginConfigured;

	public AuthenticationController(
			AppUserRepository userRepository,
			@Value("${app.auth.google.enabled:false}") boolean googleLoginConfigured) {
		this.userRepository = userRepository;
		this.googleLoginConfigured = googleLoginConfigured;
	}

	@GetMapping("/status")
	public AuthenticationStatusResponse status(@AuthenticationPrincipal OidcUser principal) {
		if (principal == null) {
			String message = googleLoginConfigured
					? null
					: "Google sign-in is not configured. Set the Google OAuth environment variables to enable it.";
			return new AuthenticationStatusResponse(false, googleLoginConfigured, message, null);
		}
		UserProfileResponse user = userRepository.findByGoogleSubject(principal.getSubject())
				.map(UserProfileResponse::from)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED, "The signed-in account is not available."));
		return new AuthenticationStatusResponse(true, googleLoginConfigured, null, user);
	}

	@GetMapping("/csrf")
	public CsrfTokenResponse csrfToken(CsrfToken csrfToken) {
		return new CsrfTokenResponse(csrfToken.getHeaderName(), csrfToken.getToken());
	}
}
