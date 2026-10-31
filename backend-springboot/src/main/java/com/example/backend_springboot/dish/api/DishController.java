package com.example.backend_springboot.dish.api;

import com.example.backend_springboot.dish.infrastructure.DishRepository;
import com.example.backend_springboot.dish.domain.Dish;
import com.example.backend_springboot.user.domain.AppUser;
import com.example.backend_springboot.user.infrastructure.AppUserRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/dishes")
public class DishController {

	private final DishRepository dishRepository;
	private final AppUserRepository userRepository;

	public DishController(DishRepository dishRepository, AppUserRepository userRepository) {
		this.dishRepository = dishRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<DishResponse> search(@RequestParam(required = false) String q) {
		String query = q == null || q.isBlank() ? null : q.trim();
		return dishRepository.search(query).stream().map(DishResponse::from).toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DishResponse create(
			@Valid @RequestBody CreateDishRequest request,
			@AuthenticationPrincipal OidcUser principal) {
		AppUser user = userRepository.findByGoogleSubject(principal.getSubject())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.UNAUTHORIZED, "The signed-in account is not available."));
		String tags = request.dietaryTags() == null ? "" : String.join(",", request.dietaryTags());
		Dish dish = Dish.create(
				request.name().trim(),
				request.description().trim(),
				request.cuisine().trim(),
				request.restaurantName().trim(),
				request.neighborhood().trim(),
				request.price(),
				request.imageUrl(),
				tags,
				user.getId());
		return DishResponse.from(dishRepository.save(dish));
	}
}