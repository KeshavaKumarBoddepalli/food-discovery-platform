package com.example.backend_springboot.user.application;

import com.example.backend_springboot.user.domain.AccountStatus;
import com.example.backend_springboot.user.domain.AppUser;
import com.example.backend_springboot.user.infrastructure.AppUserRepository;
import java.util.Map;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProvisioningService {

	private final AppUserRepository userRepository;

	public UserProvisioningService(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public AppUser provisionFromGoogle(OidcUser identity) {
		String subject = identity.getSubject();
		String email = identity.getEmail();
		String displayName = identity.getFullName();
		String profileImageUrl = identity.getPicture();
		Map<String, Object> claims = identity.getClaims();

		if (subject == null || subject.isBlank()
				|| email == null || email.isBlank()
				|| !Boolean.TRUE.equals(claims.get("email_verified"))) {
			throw authenticationError("google_identity_invalid",
					"Google must provide a subject and a verified email address.");
		}
		if (displayName == null || displayName.isBlank()) {
			displayName = email;
		}
		if (displayName.length() > 120
				|| email.length() > 320
				|| (profileImageUrl != null && profileImageUrl.length() > 2048)) {
			throw authenticationError("google_profile_invalid", "The Google profile contains unsupported values.");
		}
		String resolvedDisplayName = displayName;

		AppUser user = userRepository.findByGoogleSubject(subject)
				.orElseGet(() -> AppUser.newGoogleUser(subject, email, resolvedDisplayName, profileImageUrl));
		if (user.getStatus() != AccountStatus.ACTIVE) {
			throw authenticationError("account_unavailable", "This account is not available.");
		}

		user.updateProfile(email, resolvedDisplayName, profileImageUrl);
		return userRepository.save(user);
	}

	private OAuth2AuthenticationException authenticationError(String code, String message) {
		return new OAuth2AuthenticationException(new OAuth2Error(code, message, null), message);
	}
}
