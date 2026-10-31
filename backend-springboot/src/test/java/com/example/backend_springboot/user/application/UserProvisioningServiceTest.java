package com.example.backend_springboot.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend_springboot.user.domain.AppUser;
import com.example.backend_springboot.user.domain.UserRole;
import com.example.backend_springboot.user.infrastructure.AppUserRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceTest {

	@Mock
	private AppUserRepository userRepository;

	@Mock
	private OidcUser identity;

	private UserProvisioningService service;

	@BeforeEach
	void setUp() {
		service = new UserProvisioningService(userRepository);
		lenient().when(identity.getSubject()).thenReturn("google-subject-123");
		lenient().when(identity.getEmail()).thenReturn("taster@example.com");
		lenient().when(identity.getFullName()).thenReturn("Dish Taster");
		lenient().when(identity.getPicture()).thenReturn("https://example.com/profile.jpg");
	}

	@Test
	void createsAUserFromVerifiedGoogleIdentityWithDefaultRole() {
		when(identity.getClaims()).thenReturn(Map.of("email_verified", true));
		when(userRepository.findByGoogleSubject("google-subject-123")).thenReturn(Optional.empty());
		when(userRepository.save(org.mockito.ArgumentMatchers.any(AppUser.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		AppUser user = service.provisionFromGoogle(identity);

		assertEquals("google-subject-123", user.getGoogleSubject());
		assertEquals("taster@example.com", user.getEmail());
		assertEquals(UserRole.USER, user.getRole());
		verify(userRepository).save(user);
	}

	@Test
	void rejectsAnUnverifiedGoogleEmailBeforeDatabaseAccess() {
		when(identity.getClaims()).thenReturn(Map.of("email_verified", false));

		assertThrows(OAuth2AuthenticationException.class, () -> service.provisionFromGoogle(identity));
		verify(userRepository, never()).findByGoogleSubject(org.mockito.ArgumentMatchers.anyString());
		verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(AppUser.class));
	}

	@Test
	void refreshesProfileFieldsForTheSameGoogleSubject() {
		when(identity.getClaims()).thenReturn(Map.of("email_verified", true));
		AppUser existing = AppUser.newGoogleUser(
				"google-subject-123", "old@example.com", "Previous Name", null);
		when(userRepository.findByGoogleSubject("google-subject-123")).thenReturn(Optional.of(existing));
		when(userRepository.save(existing)).thenReturn(existing);

		AppUser user = service.provisionFromGoogle(identity);

		assertEquals("taster@example.com", user.getEmail());
		assertEquals("Dish Taster", user.getDisplayName());
		assertEquals("https://example.com/profile.jpg", user.getProfileImageUrl());
		assertEquals(UserRole.USER, user.getRole());
	}
}
