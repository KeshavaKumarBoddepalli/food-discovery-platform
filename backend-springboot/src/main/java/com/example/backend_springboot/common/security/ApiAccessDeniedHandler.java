package com.example.backend_springboot.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

	private final ApiAuthenticationEntryPoint errorWriter;

	public ApiAccessDeniedHandler(ApiAuthenticationEntryPoint errorWriter) {
		this.errorWriter = errorWriter;
	}

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			AccessDeniedException exception) throws IOException, ServletException {
		errorWriter.writeError(request, response, HttpStatus.FORBIDDEN, "ACCESS_DENIED",
				"You do not have permission to perform this action.");
	}
}
