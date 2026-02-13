package com.flashfood.flash_food;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Flash-Food Application - Food Rescue Platform
 * 
 * Main features:
 * - Redis Geo-spatial for location-based notifications
 * - High concurrency handling with distributed locks
 * - RabbitMQ for mass notifications
 * - Scheduled tasks for auto-expiry
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class FlashFoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashFoodApplication.class, args);
	}

}
