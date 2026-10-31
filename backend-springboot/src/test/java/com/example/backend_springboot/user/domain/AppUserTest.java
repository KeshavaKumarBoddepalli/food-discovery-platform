package com.example.backend_springboot.user.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppUserTest {

	@Test
	void customDisplayNameIsNotOverwrittenByGoogleProfileRefresh() {
		AppUser user = AppUser.newGoogleUser("google-subject", "person@example.com", "Google Name", null);

		user.updateDisplayName("My Food Name");
		user.updateProfile("person@example.com", "Updated Google Name", "https://example.com/photo.jpg");

		assertEquals("My Food Name", user.getDisplayName());
		assertTrue(user.isDisplayNameCustomized());
	}
}