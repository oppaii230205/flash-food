package com.flashfood.flash_food.seeder;

import com.flashfood.flash_food.entity.Category;
import com.flashfood.flash_food.entity.FoodItem;
import com.flashfood.flash_food.entity.FoodItemStatus;
import com.flashfood.flash_food.entity.Store;
import com.flashfood.flash_food.repository.CategoryRepository;
import com.flashfood.flash_food.repository.FoodItemRepository;
import com.flashfood.flash_food.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * FoodItemSeeder - generates a large dataset for load testing.
 *
 * Enabled only when app.db.seed.enabled=true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.db.seed.enabled", havingValue = "true")
public class FoodItemSeeder implements ApplicationRunner {

    private static final String DEFAULT_IMAGE_URL = "https://placehold.co/640x420?text=Food";

    private final FoodItemRepository foodItemRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.db.seed.food-items.count:50000}")
    private int targetCount;

    @Value("${app.db.seed.food-items.batch-size:1000}")
    private int batchSize;

    @Value("${app.db.seed.random-seed:42}")
    private long randomSeed;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (foodItemRepository.count() > 10) {
            log.info("FoodItemSeeder: food items already exist, skipping seeding.");
            return;
        }

        List<Store> stores = storeRepository.findAll();
        List<Category> categories = categoryRepository.findAll();

        if (stores.isEmpty() || categories.isEmpty()) {
            log.warn("FoodItemSeeder: stores or categories missing, skipping seeding.");
            return;
        }

        Random rng = new Random(randomSeed);
        LocalDateTime now = LocalDateTime.now();

        List<FoodItem> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < targetCount; i++) {
            Store store = stores.get(rng.nextInt(stores.size()));
            Category category = categories.get(rng.nextInt(categories.size()));

            FoodItem item = buildItem(i, store, category, now, rng);
            batch.add(item);

            if (batch.size() >= batchSize) {
                persistBatch(batch);
            }
        }

        if (!batch.isEmpty()) {
            persistBatch(batch);
        }

        log.info("FoodItemSeeder: seeded {} food items.", targetCount);
    }

    private void persistBatch(List<FoodItem> batch) {
        foodItemRepository.saveAll(batch);
        foodItemRepository.flush();
        entityManager.clear();
        batch.clear();
    }

    private FoodItem buildItem(int index, Store store, Category category, LocalDateTime now, Random rng) {
        String name = buildName(index, rng);
        String description = buildDescription(rng);

        int originalPrice = pickPrice(rng, 20000, 120000);
        int discountPercent = 20 + rng.nextInt(60);
        int flashPrice = Math.max(1000, originalPrice - (originalPrice * discountPercent / 100));

        int totalQuantity = 10 + rng.nextInt(140);

        StatusWindow window = buildStatusWindow(now, rng);
        int availableQuantity = window.status == FoodItemStatus.SOLD_OUT ? 0 : Math.max(0, totalQuantity - rng.nextInt(10));

        boolean expired = window.status == FoodItemStatus.EXPIRED;

        return FoodItem.builder()
                .store(store)
                .category(category)
                .name(name)
                .description(description)
                .imageUrl(DEFAULT_IMAGE_URL)
                .originalPrice(originalPrice)
                .flashPrice(flashPrice)
                .discountPercentage(discountPercent)
                .totalQuantity(totalQuantity)
                .availableQuantity(availableQuantity)
                .saleStartTime(window.start)
                .saleEndTime(window.end)
                .status(window.status)
                .isExpired(expired)
                .build();
    }

    private String buildName(int index, Random rng) {
        String[] bases = {
                "Chicken Rice", "Pork Bowl", "Veggie Bowl", "Beef Noodles",
                "Seafood Pasta", "Fried Rice", "Grilled Chicken", "Banh Mi",
                "Tofu Salad", "Shrimp Dumplings", "Pork Bun", "Spring Rolls",
                "Milk Tea", "Iced Coffee", "Fruit Juice", "Croissant",
                "Cheese Cake", "Chocolate Muffin", "Egg Tart", "Fresh Salad"
        };
        String[] suffixes = {
                "Combo", "Set", "Box", "Pack", "Special", "Value", "Family", "Mini"
        };

        String base = bases[rng.nextInt(bases.length)];
        String suffix = suffixes[rng.nextInt(suffixes.length)];
        return base + " " + suffix + " #" + (index + 1);
    }

    private String buildDescription(Random rng) {
        String[] descriptors = {
                "Freshly prepared and ready for pickup.",
                "Limited quantity, best before closing.",
                "Balanced meal with sides included.",
                "Quick bite for busy days.",
                "Chef special with seasonal ingredients.",
                "Popular choice with strong ratings.",
                "Flash sale bundle with a great discount.",
                "Light meal with clean flavors."
        };

        return descriptors[rng.nextInt(descriptors.length)];
    }

    private int pickPrice(Random rng, int min, int max) {
        int step = 1000;
        int range = (max - min) / step;
        return min + rng.nextInt(Math.max(1, range)) * step;
    }

    private StatusWindow buildStatusWindow(LocalDateTime now, Random rng) {
        int roll = rng.nextInt(100);

        if (roll < 60) {
            LocalDateTime start = now.minusHours(2 + rng.nextInt(6));
            LocalDateTime end = now.plusHours(1 + rng.nextInt(5));
            return new StatusWindow(start, end, FoodItemStatus.AVAILABLE);
        }

        if (roll < 80) {
            LocalDateTime start = now.plusHours(2 + rng.nextInt(12));
            LocalDateTime end = start.plusHours(2 + rng.nextInt(6));
            return new StatusWindow(start, end, FoodItemStatus.PENDING);
        }

        if (roll < 90) {
            LocalDateTime start = now.minusHours(10 + rng.nextInt(24));
            LocalDateTime end = now.minusHours(2 + rng.nextInt(6));
            return new StatusWindow(start, end, FoodItemStatus.EXPIRED);
        }

        LocalDateTime start = now.minusHours(1 + rng.nextInt(4));
        LocalDateTime end = now.plusHours(1 + rng.nextInt(3));
        return new StatusWindow(start, end, FoodItemStatus.SOLD_OUT);
    }

    private static final class StatusWindow {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final FoodItemStatus status;

        private StatusWindow(LocalDateTime start, LocalDateTime end, FoodItemStatus status) {
            this.start = start;
            this.end = end;
            this.status = status;
        }
    }
}
