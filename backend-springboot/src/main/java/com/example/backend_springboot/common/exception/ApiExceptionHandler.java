package com.example.backend_springboot.common.exception;

import com.example.backend_springboot.common.web.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage(),
						(first, ignored) -> first,
						LinkedHashMap::new));

		return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
				"One or more fields are invalid.", request, fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
				"The request body is missing or malformed.", request, Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
			Exception exception,
			HttpServletRequest request) {
		LOGGER.error("Unhandled API error for {}", request.getRequestURI(), exception);
		return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
				"An unexpected error occurred.", request, Map.of());
	}

	private ResponseEntity<ApiErrorResponse> response(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request,
			Map<String, String> fieldErrors) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message,
				request.getRequestURI(),
				fieldErrors);
		return ResponseEntity.status(status).body(body);
	}
}
