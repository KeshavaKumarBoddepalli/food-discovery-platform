package com.example.backend_springboot.dish.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DishTest {

	@Test
	void newDishStartsUnratedAndStoresItsContributor() {
		UUID contributorId = UUID.randomUUID();
	Dish dish = Dish.create(
				"House Dosa", "Crisp fermented rice crepe.", "South Indian", "Dosa Corner", "Indiranagar",
				new BigDecimal("120.00"), null, "Vegetarian", contributorId);

		assertEquals(BigDecimal.ZERO, dish.getRating());
		assertEquals(0, dish.getReviewCount());
		assertEquals(contributorId, dish.getCreatedById());
		assertNotNull(dish.getImageUrl());
	}
}