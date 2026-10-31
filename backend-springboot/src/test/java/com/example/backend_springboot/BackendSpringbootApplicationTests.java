package com.example.backend_springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.example.backend_springboot.dish.domain.Dish;
import com.example.backend_springboot.dish.infrastructure.DishRepository;
import com.example.backend_springboot.user.domain.AppUser;
import com.example.backend_springboot.user.infrastructure.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendSpringbootApplicationTests {

	@Autowired
	private HealthEndpoint healthEndpoint;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private DishRepository dishRepository;

	@BeforeEach
	void ensureOidcTestUserExists() {
		AppUser user = userRepository.findByGoogleSubject("integration-test-subject")
				.orElseGet(() -> userRepository.save(AppUser.newGoogleUser(
						"integration-test-subject", "test@example.com", "Google Test Name", null)));
		dishRepository.deleteAll(dishRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId()));
	}

	@Test
	void healthEndpointReportsApplicationStatus() {
		assertEquals("UP", healthEndpoint.health().getStatus().getCode());
	}

	@Test
	void loginStatusExplainsMissingGoogleConfiguration() throws Exception {
		mockMvc.perform(get("/api/v1/auth/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.authenticated").value(false))
				.andExpect(jsonPath("$.loginConfigured").value(false))
				.andExpect(jsonPath("$.loginMessage").value(
						"Google sign-in is not configured. Set the Google OAuth environment variables to enable it."));
	}

	@Test
	void protectedProfileRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/users/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
	}

	@Test
	void healthHttpResponseDoesNotExposeComponentDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components").doesNotExist());
	}

	@Test
	void dishSearchIsPublicAndMatchesDishName() throws Exception {
		mockMvc.perform(get("/api/v1/dishes").param("q", "biryani"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Hyderabadi Dum Biryani"))
				.andExpect(jsonPath("$[0].restaurantName").value("Nizam Kitchen"))
				.andExpect(jsonPath("$[0].dietaryTags[0]").value("Halal"));
	}

	@Test
	void dishSearchReturnsEmptyResultsForUnmatchedQuery() throws Exception {
		mockMvc.perform(get("/api/v1/dishes").param("q", "not-a-dish"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void authenticatedUserCanUpdateProfileAndKeepTheirChosenName() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/users/me")
					.with(SecurityMockMvcRequestPostProcessors.oidcLogin()
							.idToken(token -> token.subject("integration-test-subject")))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"displayName":"My Food Name"}
							"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("My Food Name"));
	}

	@Test
	void authenticatedDishSubmissionIsSavedToSubmittingUsersProfile() throws Exception {
		String requestBody = """
				{"name":"House Dosa","description":"Crisp fermented rice crepe.","cuisine":"South Indian", "restaurantName":"Dosa Corner", "neighborhood":"Indiranagar", "price":120.00,"dietaryTags":["Vegetarian"]}
				""";
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/dishes")
					.with(SecurityMockMvcRequestPostProcessors.oidcLogin()
							.idToken(token -> token.subject("integration-test-subject")))
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("House Dosa"))
				.andExpect(jsonPath("$.rating").value(0.0));

		AppUser user = userRepository.findByGoogleSubject("integration-test-subject").orElseThrow();
		Dish createdDish = dishRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId()).get(0);
		assertEquals(user.getId(), createdDish.getCreatedById());

		mockMvc.perform(get("/api/v1/users/me/dishes")
					.with(SecurityMockMvcRequestPostProcessors.oidcLogin()
							.idToken(token -> token.subject("integration-test-subject"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("House Dosa"));
	}

	@Test
	void anonymousUserCannotSubmitDish() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/dishes")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content("{}"))
				.andExpect(status().isUnauthorized());
	}

}
