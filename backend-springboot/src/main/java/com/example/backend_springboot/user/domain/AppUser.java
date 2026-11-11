package com.example.backend_springboot.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_user")
public class AppUser {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "google_subject", nullable = false, unique = true, updatable = false, length = 255)
	private String googleSubject;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "display_name", nullable = false, length = 120)
	private String displayName;

	@Column(name = "display_name_customized", nullable = false)
	private boolean displayNameCustomized;

	@Column(name = "profile_image_url", length = 2048)
	private String profileImageUrl;

	@Enumerated(EnumType.STRING)
	@Column(name = "application_role", nullable = false, length = 20)
	private UserRole role = UserRole.USER;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private AccountStatus status = AccountStatus.ACTIVE;

	@Column(name = "contribution_points_cache", nullable = false)
	private long contributionPointsCache;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected AppUser() {
	}

	private AppUser(String googleSubject, String email, String displayName, String profileImageUrl) {
		this.googleSubject = googleSubject;
		updateProfile(email, displayName, profileImageUrl);
	}

	public static AppUser newGoogleUser(String googleSubject, String email, String displayName, String profileImageUrl) {
		return new AppUser(googleSubject, email, displayName, profileImageUrl);
	}

	public void updateProfile(String email, String displayName, String profileImageUrl) {
		this.email = email;
		if (!displayNameCustomized) {
			this.displayName = displayName;
		}
		this.profileImageUrl = profileImageUrl;
	}

	public void updateDisplayName(String displayName) {
		this.displayName = displayName;
		this.displayNameCustomized = true;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getGoogleSubject() {
		return googleSubject;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isDisplayNameCustomized() {
		return displayNameCustomized;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public UserRole getRole() {
		return role;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public long getContributionPointsCache() {
		return contributionPointsCache;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
