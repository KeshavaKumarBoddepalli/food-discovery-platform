package com.example.backend_springboot.dish.api;

import com.example.backend_springboot.dish.domain.Dish;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record DishResponse(
		Long id,
		String name,
		String description,
		String cuisine,
		String restaurantName,
		String neighborhood,
		BigDecimal price,
		BigDecimal rating,
		int reviewCount,
		String imageUrl,
		List<String> dietaryTags) {

	public static DishResponse from(Dish dish) {
		List<String> tags = Arrays.stream(dish.getDietaryTags().split(","))
				.map(String::trim)
				.filter(tag -> !tag.isEmpty())
				.toList();
		return new DishResponse(
				dish.getId(),
				dish.getName(),
				dish.getDescription(),
				dish.getCuisine(),
				dish.getRestaurantName(),
				dish.getNeighborhood(),
				dish.getPrice(),
				dish.getRating(),
				dish.getReviewCount(),
				dish.getImageUrl(),
				tags);
	}
}