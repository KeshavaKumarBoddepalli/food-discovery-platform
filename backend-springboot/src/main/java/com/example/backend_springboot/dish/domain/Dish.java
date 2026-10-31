package com.example.backend_springboot.dish.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dish")
public class Dish {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(nullable = false, length = 800)
	private String description;

	@Column(nullable = false, length = 80)
	private String cuisine;

	@Column(name = "restaurant_name", nullable = false, length = 120)
	private String restaurantName;

	@Column(nullable = false, length = 120)
	private String neighborhood;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal price;

	@Column(nullable = false, precision = 2, scale = 1)
	private BigDecimal rating;

	@Column(name = "review_count", nullable = false)
	private int reviewCount;

	@Column(name = "image_url", nullable = false, length = 2048)
	private String imageUrl;

	@Column(name = "dietary_tags", nullable = false, length = 255)
	private String dietaryTags;

	@JdbcTypeCode(SqlTypes.BINARY)
	@Column(name = "created_by", columnDefinition = "BINARY(16)")
	private UUID createdById;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Dish() {
	}

	public static Dish create(
			String name,
			String description,
			String cuisine,
			String restaurantName,
			String neighborhood,
			BigDecimal price,
			String imageUrl,
			String dietaryTags,
			UUID createdById) {
		Dish dish = new Dish();
		dish.name = name;
		dish.description = description;
		dish.cuisine = cuisine;
		dish.restaurantName = restaurantName;
		dish.neighborhood = neighborhood;
		dish.price = price;
		dish.rating = BigDecimal.ZERO;
		dish.reviewCount = 0;
		dish.imageUrl = imageUrl == null || imageUrl.isBlank()
				? "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1000&q=85"
				: imageUrl;
		dish.dietaryTags = dietaryTags == null ? "" : dietaryTags;
		dish.createdById = createdById;
		return dish;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCuisine() {
		return cuisine;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getRating() {
		return rating;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getDietaryTags() {
		return dietaryTags;
	}

	public UUID getCreatedById() {
		return createdById;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}