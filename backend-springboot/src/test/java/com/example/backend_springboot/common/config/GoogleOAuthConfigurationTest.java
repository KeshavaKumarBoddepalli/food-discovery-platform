package com.example.backend_springboot.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

class GoogleOAuthConfigurationTest {

	private final GoogleOAuthConfiguration configuration = new GoogleOAuthConfiguration();

	@Test
	void enabledLoginRequiresAllGoogleOAuthSettings() {
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> configuration.googleClientRegistrationRepository("", "", ""));

		assertTrue(exception.getMessage().contains("GOOGLE_CLIENT_ID"));
	}

	@Test
	void registersGoogleWithTheConfiguredCallback() {
		ClientRegistrationRepository registrations = configuration.googleClientRegistrationRepository(
				"client-id",
				"client-secret",
				"https://api.example.com/login/oauth2/code/google");

		assertEquals(
				"https://api.example.com/login/oauth2/code/google",
				registrations.findByRegistrationId("google").getRedirectUri());
	}
}
