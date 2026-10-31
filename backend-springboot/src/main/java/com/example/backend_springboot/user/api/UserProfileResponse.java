package com.example.backend_springboot.user.api;

import com.example.backend_springboot.user.domain.AccountStatus;
import com.example.backend_springboot.user.domain.AppUser;
import com.example.backend_springboot.user.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
		UUID id,
		String email,
		String displayName,
		String profileImageUrl,
		UserRole role,
		AccountStatus status,
		long contributionPoints,
		Instant createdAt) {

	public static UserProfileResponse from(AppUser user) {
		return new UserProfileResponse(
				user.getId(),
				user.getEmail(),
				user.getDisplayName(),
				user.getProfileImageUrl(),
				user.getRole(),
				user.getStatus(),
				user.getContributionPointsCache(),
				user.getCreatedAt());
	}
}
