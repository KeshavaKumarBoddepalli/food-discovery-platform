package com.example.backend_springboot.common.config;

import com.example.backend_springboot.auth.infrastructure.GoogleOidcUserService;
import com.example.backend_springboot.common.security.ApiAccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;
import com.example.backend_springboot.common.security.ApiAuthenticationEntryPoint;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration(proxyBeanMethods = false)
public class GoogleOAuthConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleOAuthConfiguration.class);

	@Bean
	@ConditionalOnProperty(prefix = "app.auth.google", name = "enabled", havingValue = "true")
	ClientRegistrationRepository googleClientRegistrationRepository(
			@Value("${app.auth.google.client-id:}") String clientId,
			@Value("${app.auth.google.client-secret:}") String clientSecret,
			@Value("${app.auth.google.redirect-uri:}") String redirectUri) {
		if (clientId.isBlank() || clientSecret.isBlank() || redirectUri.isBlank()) {
			throw new IllegalStateException(
					"Google OAuth is enabled, but GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, and GOOGLE_REDIRECT_URI "
							+ "must all be configured.");
		}

		ClientRegistration registration = ClientRegistration.withRegistrationId("google")
				.clientId(clientId)
				.clientSecret(clientSecret)
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri(redirectUri)
				.scope("openid", "profile", "email")
				.authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
				.tokenUri("https://oauth2.googleapis.com/token")
				.userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
				.userNameAttributeName(IdTokenClaimNames.SUB)
				.jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
				.clientName("Google")
				.build();
		return new InMemoryClientRegistrationRepository(registration);
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.auth.google", name = "enabled", havingValue = "true")
	SecurityFilterChain googleSecurityFilterChain(
			HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource,
			ApiAuthenticationEntryPoint authenticationEntryPoint,
			ApiAccessDeniedHandler accessDeniedHandler,
			GoogleOidcUserService googleOidcUserService,
			@Value("${app.frontend-origin:http://localhost:4200}") String frontendOrigin) throws Exception {
		configureApiSecurity(http, corsConfigurationSource, authenticationEntryPoint, accessDeniedHandler);
		http.oauth2Login(oauth -> oauth
				.userInfoEndpoint(userInfo -> userInfo.oidcUserService(googleOidcUserService))
				.successHandler((request, response, authentication) -> response.sendRedirect(frontendOrigin))
				.failureHandler((request, response, exception) -> {
					String errorCode = exception instanceof OAuth2AuthenticationException oauthException
							? oauthException.getError().getErrorCode()
							: "authentication_failed";
					LOGGER.warn("Google OAuth login failed: exceptionType={}, errorCode={}",
							exception.getClass().getSimpleName(), errorCode);
					response.sendRedirect(frontendOrigin + "/?login=failed");
				}));
		http.logout(logout -> logout
				.logoutUrl("/api/v1/auth/logout")
				.logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
		return http.build();
	}

	@Bean
	@ConditionalOnProperty(prefix = "app.auth.google", name = "enabled", havingValue = "false", matchIfMissing = true)
	SecurityFilterChain localSecurityFilterChain(
			HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource,
			ApiAuthenticationEntryPoint authenticationEntryPoint,
			ApiAccessDeniedHandler accessDeniedHandler) throws Exception {
		configureApiSecurity(http, corsConfigurationSource, authenticationEntryPoint, accessDeniedHandler);
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(
			@Value("${app.frontend-origin:http://localhost:4200}") String frontendOrigin) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(frontendOrigin));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "X-Requested-With"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

	private static void configureApiSecurity(
			HttpSecurity http,
			CorsConfigurationSource corsConfigurationSource,
			ApiAuthenticationEntryPoint authenticationEntryPoint,
			ApiAccessDeniedHandler accessDeniedHandler) throws Exception {
		CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfTokenRepository.setCookiePath("/");
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.csrf(csrf -> csrf
						.csrfTokenRepository(csrfTokenRepository)
						.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/api/v1/auth/status",
								"/api/v1/auth/csrf", "/api/v1/dishes", "/api/v1/dishes/**")
						.permitAll()
						.requestMatchers("/error")
						.permitAll()
						.anyRequest()
						.authenticated());
	}
}
