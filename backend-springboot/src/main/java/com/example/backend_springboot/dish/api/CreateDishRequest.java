package com.example.backend_springboot.dish.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateDishRequest(
		@NotBlank @Size(max = 120) String name,
		@NotBlank @Size(max = 800) String description,
		@NotBlank @Size(max = 80) String cuisine,
		@NotBlank @Size(max = 120) String restaurantName,
		@NotBlank @Size(max = 120) String neighborhood,
		@NotNull @DecimalMin("0.0") @Digits(integer = 6, fraction = 2) BigDecimal price,
		@Size(max = 2048) String imageUrl,
		@Size(max = 8) List<@NotBlank @Size(max = 40) String> dietaryTags) {
}