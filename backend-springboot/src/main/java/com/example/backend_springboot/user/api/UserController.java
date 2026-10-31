package com.example.backend_springboot.user.api;

import com.example.backend_springboot.user.infrastructure.AppUserRepository;
import com.example.backend_springboot.dish.api.DishResponse;
import com.example.backend_springboot.dish.infrastructure.DishRepository;
import com.example.backend_springboot.user.domain.AppUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final AppUserRepository userRepository;
	private final DishRepository dishRepository;

	public UserController(AppUserRepository userRepository, DishRepository dishRepository) {
		this.userRepository = userRepository;
		this.dishRepository = dishRepository;
	}

	@GetMapping("/me")
	public UserProfileResponse currentUser(@AuthenticationPrincipal OidcUser principal) {
		return UserProfileResponse.from(findCurrentUser(principal));
	}

	@PutMapping("/me")
	public UserProfileResponse updateCurrentUser(
			@AuthenticationPrincipal OidcUser principal,
			@Valid @RequestBody UpdateDisplayNameRequest request) {
		AppUser user = findCurrentUser(principal);
		user.updateDisplayName(request.displayName().trim());
		return UserProfileResponse.from(userRepository.save(user));
	}

	@GetMapping("/me/dishes")
	public List<DishResponse> currentUserDishes(@AuthenticationPrincipal OidcUser principal) {
		AppUser user = findCurrentUser(principal);
		return dishRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId())
				.stream().map(DishResponse::from).toList();
	}

	private AppUser findCurrentUser(OidcUser principal) {
		return userRepository.findByGoogleSubject(principal.getSubject())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED, "The signed-in account is not available."));
	}

	public record UpdateDisplayNameRequest(@NotBlank @Size(max = 120) String displayName) {
	}
}
