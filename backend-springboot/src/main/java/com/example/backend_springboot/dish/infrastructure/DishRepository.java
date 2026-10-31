package com.example.backend_springboot.dish.infrastructure;

import com.example.backend_springboot.dish.domain.Dish;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DishRepository extends JpaRepository<Dish, Long> {

	List<Dish> findByCreatedByIdOrderByCreatedAtDesc(UUID createdById);

	@Query("""
			SELECT dish FROM Dish dish
			WHERE :query IS NULL
				OR LOWER(dish.name) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(dish.description) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(dish.cuisine) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(dish.restaurantName) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(dish.neighborhood) LIKE LOWER(CONCAT('%', :query, '%'))
				OR LOWER(dish.dietaryTags) LIKE LOWER(CONCAT('%', :query, '%'))
			ORDER BY dish.rating DESC, dish.reviewCount DESC
			""")
	List<Dish> search(@Param("query") String query);
}